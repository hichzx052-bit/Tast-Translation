const https = require('https');
const http = require('http');
const fs = require('fs');
const path = require('path');

const BOT_TOKEN = '8444095447:AAEVn6os9pPwGonoE1i8N2yXDKauNu9CFhU';
const API = `https://api.telegram.org/bot${BOT_TOKEN}`;
const OWNER_ID = 7581079032;

// ===== HTTP helper =====
function fetch(url, opts = {}) {
  return new Promise((resolve, reject) => {
    const mod = url.startsWith('https') ? https : http;
    const req = mod.get(url, { timeout: 60000, ...opts }, res => {
      if (res.statusCode >= 300 && res.statusCode < 400 && res.headers.location) {
        return fetch(res.headers.location, opts).then(resolve).catch(reject);
      }
      const chunks = [];
      res.on('data', c => chunks.push(c));
      res.on('end', () => resolve({ status: res.statusCode, data: Buffer.concat(chunks) }));
    });
    req.on('error', reject);
    req.on('timeout', () => { req.destroy(); reject(new Error('timeout')); });
  });
}

// ===== Pollinations AI =====
function imageUrl(prompt, seed) {
  const s = seed || Math.floor(Math.random() * 999999);
  return `https://image.pollinations.ai/prompt/${encodeURIComponent(prompt)}?width=1024&height=1024&nologo=true&model=flux&seed=${s}`;
}

// ===== Telegram API =====
function tg(method, body) {
  return new Promise((resolve, reject) => {
    const data = JSON.stringify(body);
    const url = new URL(`${API}/${method}`);
    const opts = {
      hostname: url.hostname, path: url.pathname, method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(data) }
    };
    const req = https.request(opts, res => {
      let d = ''; res.on('data', c => d += c);
      res.on('end', () => { try { resolve(JSON.parse(d)); } catch(e) { resolve({ok:false}); } });
    });
    req.on('error', e => { console.error('TG error:', e.message); reject(e); });
    req.write(data); req.end();
  });
}

// Send with multipart (for file upload)
function tgUpload(method, fields, filePath, fileField) {
  return new Promise((resolve, reject) => {
    const boundary = '----FormBoundary' + Math.random().toString(36).substring(2);
    let body = '';
    for (const [k, v] of Object.entries(fields)) {
      body += `--${boundary}\r\nContent-Disposition: form-data; name="${k}"\r\n\r\n${v}\r\n`;
    }
    const fileData = fs.readFileSync(filePath);
    const fileName = path.basename(filePath);
    const pre = Buffer.from(body + `--${boundary}\r\nContent-Disposition: form-data; name="${fileField}"; filename="${fileName}"\r\nContent-Type: application/octet-stream\r\n\r\n`);
    const post = Buffer.from(`\r\n--${boundary}--\r\n`);
    const payload = Buffer.concat([pre, fileData, post]);

    const url = new URL(`${API}/${method}`);
    const opts = {
      hostname: url.hostname, path: url.pathname, method: 'POST',
      headers: { 'Content-Type': `multipart/form-data; boundary=${boundary}`, 'Content-Length': payload.length }
    };
    const req = https.request(opts, res => {
      let d = ''; res.on('data', c => d += c);
      res.on('end', () => { try { resolve(JSON.parse(d)); } catch(e) { resolve({ok:false}); } });
    });
    req.on('error', reject);
    req.write(payload); req.end();
  });
}

const MAIN_KB = {
  reply_markup: {
    keyboard: [
      [{ text: '🎨 توليد صورة' }, { text: '🎬 توليد فيديو' }],
      [{ text: '🎭 الستايلات' }, { text: '⚙️ الإعدادات' }],
      [{ text: '📖 المساعدة' }, { text: '👑 لوحة الأدمن' }]
    ],
    resize_keyboard: true, is_persistent: true
  }
};

function send(chatId, text, extra = {}) {
  return tg('sendMessage', { chat_id: chatId, text, parse_mode: 'HTML', ...MAIN_KB, ...extra });
}

function answerCb(cbId, text) {
  return tg('answerCallbackQuery', { callback_query_id: cbId, text, show_alert: false });
}

// ===== User State =====
const users = {};
function getUser(id) {
  if (!users[id]) users[id] = { mode: 'image', style: '', history: 0, waitingFor: null };
  return users[id];
}

const STYLES = {
  'none': '',
  'anime': ', anime art style, vibrant colors, detailed shading',
  'realistic': ', photorealistic, 8K ultra HD, cinematic lighting, hyperdetailed',
  'fantasy': ', epic fantasy art, dramatic lighting, magical atmosphere, 4K',
  'cartoon': ', cartoon style, colorful, fun, clean lines, Pixar',
  'oil': ', oil painting style, classical art, rich textures, masterpiece',
  'cyber': ', cyberpunk style, neon lights, futuristic city, dark atmosphere',
  '3d': ', 3D render, Unreal Engine, high quality, detailed textures',
};
const STYLE_NAMES = {
  'none': '🎨 بدون', 'anime': '🌸 أنمي', 'realistic': '📸 واقعي',
  'fantasy': '⚔️ فانتازي', 'cartoon': '🎪 كرتوني', 'oil': '🖌️ زيتي',
  'cyber': '🌃 سايبربنك', '3d': '🎮 3D',
};

// ===== Generate Video = 4 scene images =====
async function generateVideo(chatId, prompt, user) {
  const statusMsg = await send(chatId,
    `🎬 <b>جارٍ توليد مشاهد الفيديو...</b>\n\n` +
    `📝 <i>${prompt.substring(0, 100)}</i>\n\n` +
    `⏳ يولّد 4 مشاهد متتابعة...`);

  try {
    const scenes = [
      `${prompt}, scene 1 establishing shot, wide angle, cinematic`,
      `${prompt}, scene 2 medium shot, action moment, dramatic`,
      `${prompt}, scene 3 close up detail, emotional, intense`,
      `${prompt}, scene 4 epic finale, wide shot, golden hour lighting`,
    ];

    const media = scenes.map((s, i) => ({
      type: 'photo',
      media: imageUrl(s + (STYLES[user.style] || ''), 1000 + i),
      caption: i === 0 ? `🎬 <b>مشاهد الفيديو</b>\n\n📝 <i>${prompt.substring(0, 150)}</i>\n\n🎭 ${STYLE_NAMES[user.style || 'none']}\n\n⚡ <b>Hichamdzz AI Studio</b>` : '',
      parse_mode: i === 0 ? 'HTML' : undefined,
    }));

    await tg('sendMediaGroup', { chat_id: chatId, media });
    tg('deleteMessage', { chat_id: chatId, message_id: statusMsg?.result?.message_id }).catch(() => {});
  } catch(err) {
    console.error('Video error:', err.message);
    tg('editMessageText', {
      chat_id: chatId, message_id: statusMsg?.result?.message_id,
      text: `❌ <b>خطأ!</b>\n${err.message}\n\nجرب مرة ثانية`, parse_mode: 'HTML'
    }).catch(() => {});
  }
}

// ===== Generate Image =====
async function generateImage(chatId, prompt, user) {
  const statusMsg = await send(chatId,
    `🎨 <b>جارٍ التوليد...</b>\n\n` +
    `📝 <i>${prompt.substring(0, 100)}</i>\n` +
    `🎭 ${STYLE_NAMES[user.style || 'none']}\n\n` +
    `⏳ 10-30 ثانية...`);

  try {
    const fullPrompt = prompt + (STYLES[user.style] || '');
    const url = imageUrl(fullPrompt);

    // Download image first for reliability
    console.log('Downloading image...');
    const imgRes = await fetch(url);
    if (imgRes.status !== 200) throw new Error(`Image API returned ${imgRes.status}`);

    const tmpPath = `/tmp/img_${Date.now()}.jpg`;
    fs.writeFileSync(tmpPath, imgRes.data);
    console.log(`Image saved: ${tmpPath} (${imgRes.data.length} bytes)`);

    const promptB64 = Buffer.from(prompt.substring(0, 60)).toString('base64');
    const inlineKb = JSON.stringify({ inline_keyboard: [
      [{ text: '🔄 نتيجة ثانية', callback_data: `regen:${promptB64}` }],
    ]});

    const res = await tgUpload('sendPhoto', {
      chat_id: chatId,
      caption: `✨ <b>تم!</b>\n\n📝 <i>${prompt.substring(0, 200)}</i>\n🎭 ${STYLE_NAMES[user.style || 'none']}\n\n⚡ <b>Hichamdzz AI Studio</b>`,
      parse_mode: 'HTML',
      reply_markup: inlineKb,
    }, tmpPath, 'photo');

    fs.unlinkSync(tmpPath);

    if (!res.ok) {
      console.error('SendPhoto failed:', JSON.stringify(res));
      // Fallback: send by URL
      await tg('sendPhoto', {
        chat_id: chatId, photo: url,
        caption: `✨ <b>تم!</b>\n\n📝 <i>${prompt.substring(0, 200)}</i>\n\n⚡ <b>Hichamdzz AI Studio</b>`,
        parse_mode: 'HTML',
      });
    }

    tg('deleteMessage', { chat_id: chatId, message_id: statusMsg?.result?.message_id }).catch(() => {});
    console.log('Image sent successfully!');
  } catch(err) {
    console.error('Image error:', err.message);
    tg('editMessageText', {
      chat_id: chatId, message_id: statusMsg?.result?.message_id,
      text: `❌ <b>خطأ!</b>\n${err.message}\n\nجرب مرة ثانية`, parse_mode: 'HTML'
    }).catch(() => {});
  }
}

// ===== Handle Messages =====
async function handleMessage(msg) {
  const chatId = msg.chat.id;
  const text = (msg.text || '').trim();
  const name = msg.from?.first_name || 'صديقي';
  const user = getUser(chatId);

  console.log(`[MSG] ${chatId}: "${text}"`);

  if (text === '🎨 توليد صورة' || text === '/image') {
    user.mode = 'image';
    user.waitingFor = 'image_prompt';
    return send(chatId, `🎨 <b>وضع الصور</b>\n\n📝 أرسل وصف الصورة:\n\n💡 <i>مثال: A warrior with golden armor</i>`);
  }

  if (text === '🎬 توليد فيديو' || text === '/video') {
    user.mode = 'video';
    user.waitingFor = 'video_prompt';
    return send(chatId, `🎬 <b>وضع الفيديو</b>\n\n📝 أرسل وصف الفيديو:\n(يولّد 4 مشاهد متتابعة)\n\n💡 <i>مثال: A dragon flying over mountains</i>`);
  }

  if (text === '🎭 الستايلات' || text === '/styles') {
    const btns = Object.entries(STYLE_NAMES).map(([k, v]) => {
      const check = (user.style === k || (!user.style && k === 'none')) ? ' ✅' : '';
      return [{ text: v + check, callback_data: `style_${k}` }];
    });
    return tg('sendMessage', {
      chat_id: chatId, text: `🎭 <b>اختر الستايل:</b>`, parse_mode: 'HTML',
      reply_markup: { inline_keyboard: btns }
    });
  }

  if (text === '⚙️ الإعدادات' || text === '/settings') {
    return send(chatId,
      `⚙️ <b>الإعدادات</b>\n\n` +
      `📌 الوضع: <b>${user.mode === 'video' ? '🎬 فيديو' : '🎨 صور'}</b>\n` +
      `🎭 الستايل: <b>${STYLE_NAMES[user.style || 'none']}</b>\n` +
      `📊 التوليدات: <b>${user.history}</b>\n` +
      `👤 آيدي: <code>${chatId}</code>`);
  }

  if (text === '📖 المساعدة' || text === '/help' || text === '/start') {
    user.waitingFor = null;
    return send(chatId,
      `✨ <b>Hichamdzz AI Studio</b> ✨\n\n` +
      `مرحباً <b>${name}</b>! 👋\n\n` +
      `🎨 <b>توليد صورة</b> — اضغط الزر + أرسل الوصف\n` +
      `🎬 <b>توليد فيديو</b> — اضغط الزر + أرسل الوصف\n` +
      `🎭 <b>الستايلات</b> — غيّر شكل الصور\n\n` +
      `⚡ مجاني — بدون حدود\n` +
      `👨‍💻 Developer: <b>Hichamdzz</b>`);
  }

  if (text === '👑 لوحة الأدمن') {
    if (chatId !== OWNER_ID) return send(chatId, `🔒 للأدمن فقط!`);
    const uc = Object.keys(users).length;
    const tg2 = Object.values(users).reduce((s, u) => s + u.history, 0);
    return send(chatId,
      `👑 <b>لوحة الأدمن</b>\n\n` +
      `👥 مستخدمين: <b>${uc}</b>\n` +
      `📊 توليدات: <b>${tg2}</b>\n` +
      `🤖 الحالة: <b>✅ شغال</b>\n` +
      `🎨 محرك: <b>Pollinations AI (Flux)</b>`);
  }

  if (!text || text.startsWith('/')) return;

  user.history++;

  if (user.mode === 'video' || user.waitingFor === 'video_prompt') {
    user.waitingFor = null;
    return generateVideo(chatId, text, user);
  } else {
    user.waitingFor = null;
    return generateImage(chatId, text, user);
  }
}

// ===== Callbacks =====
async function handleCallback(cb) {
  const chatId = cb.message.chat.id;
  const data = cb.data;
  const user = getUser(chatId);
  console.log(`[CB] ${chatId}: ${data}`);

  if (data.startsWith('style_')) {
    const style = data.replace('style_', '');
    user.style = style === 'none' ? '' : style;
    answerCb(cb.id, `✅ ${STYLE_NAMES[style]}`);
    send(chatId, `✅ الستايل: <b>${STYLE_NAMES[style]}</b>\n\nأرسل الوصف!`);
  } else if (data.startsWith('regen:')) {
    answerCb(cb.id, '🔄');
    try {
      const prompt = Buffer.from(data.split(':')[1], 'base64').toString();
      generateImage(chatId, prompt, user);
    } catch(e) { send(chatId, '🔄 أرسل الوصف مرة ثانية'); }
  } else {
    answerCb(cb.id, '👍');
  }
}

// ===== Polling =====
let offset = 0;
async function poll() {
  try {
    const res = await tg('getUpdates', { offset, timeout: 30, allowed_updates: ['message', 'callback_query'] });
    if (res.ok && res.result?.length) {
      for (const upd of res.result) {
        offset = upd.update_id + 1;
        try {
          if (upd.message) await handleMessage(upd.message);
          if (upd.callback_query) await handleCallback(upd.callback_query);
        } catch(e) { console.error('Handler err:', e.message); }
      }
    }
  } catch(e) {
    console.error('Poll err:', e.message);
    await new Promise(r => setTimeout(r, 5000));
  }
  setImmediate(poll);
}

tg('deleteWebhook', {}).then(() => {
  console.log('✨ Hichamdzz AI Studio Bot v2 started!');
  console.log('🎨 Image (download+upload) + 🎬 Video (4 scenes)');
  poll();
});
