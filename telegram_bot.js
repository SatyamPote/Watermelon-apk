const { Telegraf, Markup } = require('telegraf');
const { createClient } = require('@supabase/supabase-js');

const BOT_TOKEN = process.env.TELEGRAM_BOT_TOKEN || 'YOUR_BOT_TOKEN_HERE';
const ADMIN_CHAT_ID = process.env.TELEGRAM_ADMIN_CHAT_ID || 'YOUR_ADMIN_CHAT_ID';
const SUPABASE_URL = process.env.SUPABASE_URL || 'https://xljlceoircpibojirxob.supabase.co';
const SUPABASE_SERVICE_KEY = process.env.SUPABASE_SERVICE_KEY || 'YOUR_SERVICE_ROLE_KEY';

const bot = new Telegraf(BOT_TOKEN);
const supabase = createClient(SUPABASE_URL, SUPABASE_SERVICE_KEY, {
  auth: { autoRefreshToken: false, persistSession: false }
});

function isAdmin(ctx) {
  return String(ctx.chat?.id || ctx.from?.id) === String(ADMIN_CHAT_ID);
}

const mainKeyboard = Markup.keyboard([
  ['👥 Users', '📊 Stats'],
  ['📅 Daily', '⏳ Pending'],
  ['🏆 Top Users', '🆕 Recent'],
  ['📈 Retention', '🎵 Plays']
]).resize();

// ========== START ==========

bot.start((ctx) => {
  if (!isAdmin(ctx)) return ctx.reply('Unauthorized.');
  ctx.reply(
    '🍉 <b>Watermelon Admin Bot</b>\n\n' +
    'Use the menu below or type commands:\n' +
    '/users — Total, free & paid users\n' +
    '/subs — Recent premium subscribers\n' +
    '/plays — Total plays & top songs\n' +
    '/daily — Plays & signups in last 24h\n' +
    '/topusers — Most active users\n' +
    '/recent — New signups today\n' +
    '/retention — Active users (7d / 30d)\n' +
    '/pending — Pending premium requests\n' +
    '/stats — Combined dashboard\n' +
    '/verify &lt;email&gt; — Approve premium\n' +
    '/revoke &lt;email&gt; — Revoke premium',
    { parse_mode: 'HTML', ...mainKeyboard }
  );
});

// ========== KEYBOARD HANDLERS ==========

bot.hears('👥 Users', (ctx) => ctx.reply('/users'));
bot.hears('📊 Stats', (ctx) => ctx.reply('/stats'));
bot.hears('📅 Daily', (ctx) => ctx.reply('/daily'));
bot.hears('⏳ Pending', (ctx) => ctx.reply('/pending'));
bot.hears('🏆 Top Users', (ctx) => ctx.reply('/topusers'));
bot.hears('🆕 Recent', (ctx) => ctx.reply('/recent'));
bot.hears('📈 Retention', (ctx) => ctx.reply('/retention'));
bot.hears('🎵 Plays', (ctx) => ctx.reply('/plays'));

// ========== COMMANDS ==========

bot.command('users', async (ctx) => {
  if (!isAdmin(ctx)) return;
  try {
    const { count: total, error: e1 } = await supabase.from('profiles').select('*', { count: 'exact', head: true });
    const { count: free, error: e2 } = await supabase.from('profiles').select('*', { count: 'exact', head: true }).eq('plan', 'FREE');
    const { count: paid, error: e3 } = await supabase.from('profiles').select('*', { count: 'exact', head: true }).neq('plan', 'FREE');
    if (e1 || e2 || e3) throw e1 || e2 || e3;
    ctx.reply(
      `<b>👥 Users</b>\n` +
      `Total: <b>${total}</b>\n` +
      `Free: <b>${free}</b>\n` +
      `Paid: <b>${paid}</b>`,
      { parse_mode: 'HTML', ...mainKeyboard }
    );
  } catch (e) {
    ctx.reply(`Error: ${e.message}`, mainKeyboard);
  }
});

bot.command('subs', async (ctx) => {
  if (!isAdmin(ctx)) return;
  try {
    const { data, error } = await supabase.from('profiles')
      .select('email, plan, created_at')
      .neq('plan', 'FREE')
      .order('created_at', { ascending: false })
      .limit(20);
    if (error) throw error;
    const lines = data.map(u => `• ${u.email} | ${u.plan} | ${u.created_at?.slice(0,10)||''}`).join('\n');
    ctx.reply(`<b>💎 Recent Premium Users</b>\n\n${lines || 'None'}`, { parse_mode: 'HTML', ...mainKeyboard });
  } catch (e) {
    ctx.reply(`Error: ${e.message}`, mainKeyboard);
  }
});

bot.command('plays', async (ctx) => {
  if (!isAdmin(ctx)) return;
  try {
    const { count: totalPlays, error: e1 } = await supabase.from('listening_history').select('*', { count: 'exact', head: true });
    const { count: todayPlays, error: e2 } = await supabase.from('listening_history').select('*', { count: 'exact', head: true }).gte('played_at', new Date(Date.now() - 864e5).toISOString());
    const { data: topSongs, error: e3 } = await supabase.rpc('get_top_songs', { limit_n: 5 });
    if (e1 || e2) throw e1 || e2;
    let top = '';
    if (topSongs && topSongs.length) {
      top = '\n\n<b>Top Songs:</b>\n' + topSongs.map((s, i) => `${i+1}. ${s.title} (${s.plays})`).join('\n');
    }
    ctx.reply(
      `<b>🎵 Plays</b>\nTotal: <b>${totalPlays}</b>\nLast 24h: <b>${todayPlays}</b>${top}`,
      { parse_mode: 'HTML', ...mainKeyboard }
    );
  } catch (e) {
    ctx.reply(`Error: ${e.message}`, mainKeyboard);
  }
});

bot.command('verify', async (ctx) => {
  if (!isAdmin(ctx)) return;
  const email = ctx.message.text.split(' ').slice(1).join(' ').trim();
  if (!email) return ctx.reply('Usage: /verify user@email.com', mainKeyboard);
  try {
    const { data: user, error: e1 } = await supabase.from('profiles')
      .select('id, email, plan')
      .eq('email', email)
      .single();
    if (e1 || !user) return ctx.reply('User not found.', mainKeyboard);
    const { error: e2 } = await supabase.from('profiles').update({ plan: 'PREMIUM_INDIVIDUAL' }).eq('id', user.id);
    if (e2) throw e2;
    ctx.reply(`✅ <b>Verified</b>\n${user.email} → <b>PREMIUM_INDIVIDUAL</b>`, { parse_mode: 'HTML', ...mainKeyboard });
  } catch (e) {
    ctx.reply(`Error: ${e.message}`, mainKeyboard);
  }
});

bot.command('revoke', async (ctx) => {
  if (!isAdmin(ctx)) return;
  const email = ctx.message.text.split(' ').slice(1).join(' ').trim();
  if (!email) return ctx.reply('Usage: /revoke user@email.com', mainKeyboard);
  try {
    const { data: user, error: e1 } = await supabase.from('profiles')
      .select('id, email, plan')
      .eq('email', email)
      .single();
    if (e1 || !user) return ctx.reply('User not found.', mainKeyboard);
    const { error: e2 } = await supabase.from('profiles').update({ plan: 'FREE' }).eq('id', user.id);
    if (e2) throw e2;
    ctx.reply(`⛔ <b>Revoked</b>\n${user.email} → <b>FREE</b>`, { parse_mode: 'HTML', ...mainKeyboard });
  } catch (e) {
    ctx.reply(`Error: ${e.message}`, mainKeyboard);
  }
});

bot.command('stats', async (ctx) => {
  if (!isAdmin(ctx)) return;
  try {
    const { count: users } = await supabase.from('profiles').select('*', { count: 'exact', head: true });
    const { count: free } = await supabase.from('profiles').select('*', { count: 'exact', head: true }).eq('plan', 'FREE');
    const { count: paid } = await supabase.from('profiles').select('*', { count: 'exact', head: true }).neq('plan', 'FREE');
    const { count: playlists } = await supabase.from('playlists').select('*', { count: 'exact', head: true });
    const { count: favorites } = await supabase.from('favorites').select('*', { count: 'exact', head: true });
    const { count: plays } = await supabase.from('listening_history').select('*', { count: 'exact', head: true });
    const { count: todayPlays } = await supabase.from('listening_history').select('*', { count: 'exact', head: true }).gte('played_at', new Date(Date.now() - 864e5).toISOString());
    ctx.reply(
      `<b>📊 Dashboard</b>\n` +
      `👥 Total: <b>${users}</b> (Free ${free}, Paid ${paid})\n` +
      `📋 Playlists: <b>${playlists}</b> | ⭐ Favs: <b>${favorites}</b>\n` +
      `🎵 Total plays: <b>${plays}</b>\n` +
      `🕐 Plays (24h): <b>${todayPlays}</b>`,
      { parse_mode: 'HTML', ...mainKeyboard }
    );
  } catch (e) {
    ctx.reply(`Error: ${e.message}`, mainKeyboard);
  }
});

bot.command('daily', async (ctx) => {
  if (!isAdmin(ctx)) return;
  try {
    const yesterday = new Date(Date.now() - 864e5).toISOString();
    const { count: todayPlays } = await supabase.from('listening_history').select('*', { count: 'exact', head: true }).gte('played_at', yesterday);
    const { count: todaySignups } = await supabase.from('profiles').select('*', { count: 'exact', head: true }).gte('created_at', yesterday);
    const { data: topSong } = await supabase.from('listening_history').select('title, artist').gte('played_at', yesterday).limit(1).order('played_at', { ascending: false });
    const top = topSong?.[0] ? `${topSong[0].title} — ${topSong[0].artist || 'Unknown'}` : 'No plays yet';
    ctx.reply(
      `<b>📅 Daily Digest (last 24h)</b>\n` +
      `🎵 Plays: <b>${todayPlays}</b>\n` +
      `🆕 Signups: <b>${todaySignups}</b>\n` +
      `🔥 Top song: <i>${top}</i>`,
      { parse_mode: 'HTML', ...mainKeyboard }
    );
  } catch (e) {
    ctx.reply(`Error: ${e.message}`, mainKeyboard);
  }
});

bot.command('topusers', async (ctx) => {
  if (!isAdmin(ctx)) return;
  try {
    const { data, error } = await supabase.from('listening_history').select('user_id, title').order('played_at', { ascending: false }).limit(500);
    if (error) throw error;
    const counts = {};
    data.forEach(d => { counts[d.user_id] = (counts[d.user_id] || 0) + 1; });
    const top = Object.entries(counts).sort((a, b) => b[1] - a[1]).slice(0, 10).map(([uid, c]) => ({ uid, count: c }));
    if (!top.length) return ctx.reply('No plays yet.', mainKeyboard);
    const ids = top.map(t => t.uid);
    const { data: users, error: ue } = await supabase.from('profiles').select('id, email, display_name').in('id', ids);
    if (ue) throw ue;
    const lines = top.map((t, i) => {
      const u = users?.find(u => u.id === t.uid);
      const name = u?.display_name || u?.email || t.uid.slice(0, 8);
      return `${i + 1}. <b>${name}</b> — ${t.count} plays`;
    }).join('\n');
    ctx.reply(`<b>🏆 Top Users (by plays)</b>\n\n${lines}`, { parse_mode: 'HTML', ...mainKeyboard });
  } catch (e) {
    ctx.reply(`Error: ${e.message}`, mainKeyboard);
  }
});

bot.command('recent', async (ctx) => {
  if (!isAdmin(ctx)) return;
  try {
    const yesterday = new Date(Date.now() - 864e5).toISOString();
    const { data, error } = await supabase.from('profiles').select('email, display_name, created_at').gte('created_at', yesterday).order('created_at', { ascending: false }).limit(20);
    if (error) throw error;
    if (!data.length) return ctx.reply('No new signups today.', mainKeyboard);
    const lines = data.map(u => `• <b>${u.display_name || u.email}</b> — ${u.created_at?.slice(0, 10)}`).join('\n');
    ctx.reply(`<b>🆕 Recent Signups (24h)</b>\n\n${lines}`, { parse_mode: 'HTML', ...mainKeyboard });
  } catch (e) {
    ctx.reply(`Error: ${e.message}`, mainKeyboard);
  }
});

bot.command('retention', async (ctx) => {
  if (!isAdmin(ctx)) return;
  try {
    const d7 = new Date(Date.now() - 7 * 864e5).toISOString();
    const d30 = new Date(Date.now() - 30 * 864e5).toISOString();
    const { data: week } = await supabase.from('listening_history').select('user_id').gte('played_at', d7);
    const { data: month } = await supabase.from('listening_history').select('user_id').gte('played_at', d30);
    const active7 = week ? new Set(week.map(r => r.user_id)).size : 0;
    const active30 = month ? new Set(month.map(r => r.user_id)).size : 0;
    const { count: total } = await supabase.from('profiles').select('*', { count: 'exact', head: true });
    ctx.reply(
      `<b>📈 Retention</b>\n` +
      `👥 Total: <b>${total}</b>\n` +
      `🕐 Active (7d): <b>${active7}</b>\n` +
      `🕐 Active (30d): <b>${active30}</b>`,
      { parse_mode: 'HTML', ...mainKeyboard }
    );
  } catch (e) {
    ctx.reply(`Error: ${e.message}`, mainKeyboard);
  }
});

bot.command('pending', async (ctx) => {
  if (!isAdmin(ctx)) return;
  try {
    const { data, error } = await supabase.from('premium_requests').select('*').eq('status', 'pending').order('created_at', { ascending: false }).limit(10);
    if (error) throw error;
    if (!data.length) return ctx.reply('⏳ No pending requests.', mainKeyboard);
    
    for (const r of data) {
      const inlineKeyboard = Markup.inlineKeyboard([
        Markup.button.callback('✅ Approve', `approve_${r.id}`),
        Markup.button.callback('❌ Reject', `reject_${r.id}`)
      ]);
      await ctx.reply(
        `<b>⏳ Pending Request</b>\n` +
        `Email: <code>${r.email}</code>\n` +
        `Plan: <b>${r.plan}</b>\n` +
        `Amount: ₹${r.amount / 100}\n` +
        `Date: ${r.created_at?.slice(0, 10)}`,
        { parse_mode: 'HTML', ...inlineKeyboard }
      );
    }
  } catch (e) {
    ctx.reply(`Error: ${e.message}`, mainKeyboard);
  }
});

// ========== INLINE CALLBACKS ==========

bot.action(/approve_(.+)/, async (ctx) => {
  if (!isAdmin(ctx)) return;
  const id = ctx.match[1];
  try {
    const { data: req, error: e1 } = await supabase.from('premium_requests').select('*').eq('id', id).single();
    if (e1 || !req) return ctx.answerCbQuery('Request not found.');
    const { data: profile } = await supabase.from('profiles').select('id').eq('email', req.email).single();
    if (profile) {
      await supabase.from('profiles').update({ plan: req.plan }).eq('id', profile.id);
    }
    await supabase.from('premium_requests').update({ status: 'approved', updated_at: new Date().toISOString() }).eq('id', id);
    await ctx.editMessageText(
      `✅ <b>Approved</b>\n${req.email} → <b>${req.plan}</b>`,
      { parse_mode: 'HTML' }
    );
    await ctx.answerCbQuery('Approved!');
  } catch (e) {
    ctx.answerCbQuery(`Error: ${e.message}`);
  }
});

bot.action(/reject_(.+)/, async (ctx) => {
  if (!isAdmin(ctx)) return;
  const id = ctx.match[1];
  try {
    const { data: req, error: e1 } = await supabase.from('premium_requests').select('*').eq('id', id).single();
    if (e1 || !req) return ctx.answerCbQuery('Request not found.');
    await supabase.from('premium_requests').update({ status: 'rejected', updated_at: new Date().toISOString() }).eq('id', id);
    await ctx.editMessageText(
      `❌ <b>Rejected</b>\n${req.email}`,
      { parse_mode: 'HTML' }
    );
    await ctx.answerCbQuery('Rejected.');
  } catch (e) {
    ctx.answerCbQuery(`Error: ${e.message}`);
  }
});

bot.launch();
console.log('Watermelon Telegram admin bot started');

process.once('SIGINT', () => bot.stop('SIGINT'));
process.once('SIGTERM', () => bot.stop('SIGTERM'));
