const { Telegraf } = require('telegraf');
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

// ========== COMMANDS ==========

bot.start((ctx) => {
  if (!isAdmin(ctx)) return ctx.reply('Unauthorized.');
  ctx.reply(
    '🍉 Watermelon Admin Bot\n\n' +
    'Commands:\n' +
    '/users — Total, free & paid users\n' +
    '/subs — Recent premium subscribers\n' +
    '/plays — Total plays & top songs\n' +
    '/daily — Plays & signups in last 24h\n' +
    '/topusers — Most active users\n' +
    '/recent — New signups today\n' +
    '/retention — Active users (7d / 30d)\n' +
    '/pending — Pending premium requests\n' +
    '/stats — Combined dashboard\n' +
    '/verify <email> — Approve premium\n' +
    '/revoke <email> — Revoke premium'
  );
});

bot.command('users', async (ctx) => {
  if (!isAdmin(ctx)) return;
  try {
    const { count: total, error: e1 } = await supabase.from('profiles').select('*', { count: 'exact', head: true });
    const { count: free, error: e2 } = await supabase.from('profiles').select('*', { count: 'exact', head: true }).eq('plan', 'FREE');
    const { count: paid, error: e3 } = await supabase.from('profiles').select('*', { count: 'exact', head: true }).neq('plan', 'FREE');
    if (e1 || e2 || e3) throw e1 || e2 || e3;
    ctx.reply(`👥 Users\nTotal: ${total}\nFree: ${free}\nPaid: ${paid}`);
  } catch (e) {
    ctx.reply(`Error: ${e.message}`);
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
    ctx.reply(`💎 Recent Premium Users\n\n${lines || 'None'}`);
  } catch (e) {
    ctx.reply(`Error: ${e.message}`);
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
      top = '\n\nTop Songs:\n' + topSongs.map((s, i) => `${i+1}. ${s.title} (${s.plays})`).join('\n');
    }
    ctx.reply(`🎵 Plays\nTotal: ${totalPlays}\nLast 24h: ${todayPlays}${top}`);
  } catch (e) {
    ctx.reply(`Error: ${e.message}`);
  }
});

bot.command('verify', async (ctx) => {
  if (!isAdmin(ctx)) return;
  const email = ctx.message.text.split(' ').slice(1).join(' ').trim();
  if (!email) return ctx.reply('Usage: /verify user@email.com');
  try {
    const { data: user, error: e1 } = await supabase.from('profiles')
      .select('id, email, plan')
      .eq('email', email)
      .single();
    if (e1 || !user) return ctx.reply('User not found.');
    const { error: e2 } = await supabase.from('profiles').update({ plan: 'PREMIUM_INDIVIDUAL' }).eq('id', user.id);
    if (e2) throw e2;
    ctx.reply(`✅ Verified\n${user.email} → PREMIUM_INDIVIDUAL`);
  } catch (e) {
    ctx.reply(`Error: ${e.message}`);
  }
});

bot.command('revoke', async (ctx) => {
  if (!isAdmin(ctx)) return;
  const email = ctx.message.text.split(' ').slice(1).join(' ').trim();
  if (!email) return ctx.reply('Usage: /revoke user@email.com');
  try {
    const { data: user, error: e1 } = await supabase.from('profiles')
      .select('id, email, plan')
      .eq('email', email)
      .single();
    if (e1 || !user) return ctx.reply('User not found.');
    const { error: e2 } = await supabase.from('profiles').update({ plan: 'FREE' }).eq('id', user.id);
    if (e2) throw e2;
    ctx.reply(`⛔ Revoked\n${user.email} → FREE`);
  } catch (e) {
    ctx.reply(`Error: ${e.message}`);
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
    ctx.reply(`📊 Dashboard\n👥 Total: ${users} (Free ${free}, Paid ${paid})\n📋 Playlists: ${playlists} | ⭐ Favs: ${favorites}\n🎵 Total plays: ${plays}\n🕐 Plays (24h): ${todayPlays}`);
  } catch (e) {
    ctx.reply(`Error: ${e.message}`);
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
    ctx.reply(`📅 Daily Digest (last 24h)\n🎵 Plays: ${todayPlays}\n🆕 Signups: ${todaySignups}\n🔥 Top song: ${top}`);
  } catch (e) {
    ctx.reply(`Error: ${e.message}`);
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
    if (!top.length) return ctx.reply('No plays yet.');
    const ids = top.map(t => t.uid);
    const { data: users, error: ue } = await supabase.from('profiles').select('id, email, display_name').in('id', ids);
    if (ue) throw ue;
    const lines = top.map((t, i) => {
      const u = users?.find(u => u.id === t.uid);
      const name = u?.display_name || u?.email || t.uid.slice(0, 8);
      return `${i + 1}. ${name} — ${t.count} plays`;
    }).join('\n');
    ctx.reply(`🏆 Top Users (by plays)\n\n${lines}`);
  } catch (e) {
    ctx.reply(`Error: ${e.message}`);
  }
});

bot.command('recent', async (ctx) => {
  if (!isAdmin(ctx)) return;
  try {
    const yesterday = new Date(Date.now() - 864e5).toISOString();
    const { data, error } = await supabase.from('profiles').select('email, display_name, created_at').gte('created_at', yesterday).order('created_at', { ascending: false }).limit(20);
    if (error) throw error;
    if (!data.length) return ctx.reply('No new signups today.');
    const lines = data.map(u => `• ${u.display_name || u.email} — ${u.created_at?.slice(0, 10)}`).join('\n');
    ctx.reply(`🆕 Recent Signups (24h)\n\n${lines}`);
  } catch (e) {
    ctx.reply(`Error: ${e.message}`);
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
    ctx.reply(`📈 Retention\n👥 Total: ${total}\n🕐 Active (7d): ${active7}\n🕐 Active (30d): ${active30}`);
  } catch (e) {
    ctx.reply(`Error: ${e.message}`);
  }
});

bot.command('pending', async (ctx) => {
  if (!isAdmin(ctx)) return;
  try {
    const { data, error } = await supabase.from('premium_requests').select('*').eq('status', 'pending').order('created_at', { ascending: false }).limit(20);
    if (error) throw error;
    if (!data.length) return ctx.reply('No pending requests.');
    const lines = data.map(r => `• ${r.email} | ${r.plan} | ₹${r.amount / 100} — ${r.created_at?.slice(0, 10)}`).join('\n');
    ctx.reply(`⏳ Pending Premium Requests\n\n${lines}`);
  } catch (e) {
    ctx.reply(`Error: ${e.message}`);
  }
});

bot.launch();
console.log('Watermelon Telegram admin bot started');

// Graceful stop
process.once('SIGINT', () => bot.stop('SIGINT'));
process.once('SIGTERM', () => bot.stop('SIGTERM'));
