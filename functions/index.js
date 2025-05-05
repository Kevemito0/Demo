// functions/index.js
const admin = require('firebase-admin');
const { setGlobalOptions } = require('firebase-functions');
const {
  onValueWritten,
  onValueUpdated
} = require('firebase-functions/v2/database');
const { onDocumentUpdated } = require('firebase-functions/v2/firestore');

admin.initializeApp();
setGlobalOptions({ region: 'us-central1' });

/**
 * 1️⃣ RealtimeDB’deki /sensors yoluna gelen verilerden
 * sadece uyarı koşulları sağlandığında Firestore’a yaz:
 *   • temperature > 50
 *   • motion === true
 *   • gas === 1
 */
exports.syncSensorData = onValueWritten(
  { ref: '/sensors' },
  async (event) => {
    // önceki ve yeni snapshot
    const beforeSnap = event.data.before;
    const afterSnap  = event.data.after;

    const before = beforeSnap.exists() ? beforeSnap.val() : {};
    const after  = afterSnap.exists()  ? afterSnap.val()  : {};

    // değerleri al
    const prevTemp     = Number(before.temperature  || 0);
    const temp         = Number(after.temperature   || 0);

    const prevMotion   = Boolean(before.motion);
    const motion       = Boolean(after.motion);

    const prevGas      = before.gas         || false;
    const gas          = after.gas          || false;

    const prevDoorLock = Boolean(before.doorLock);
    const doorLock     = Boolean(after.doorLock);

    // tetikleme koşulları
    const tempTriggered     = prevTemp < 50   && temp >= 50;
    const motionTriggered   = !prevMotion      && motion;
    const gasTriggered      = prevGas !== true    && gas === true;
    const doorLockTriggered = !prevDoorLock    && doorLock;

    // hiçbir sensör tetiklenmediyse çık
    if (!(tempTriggered || motionTriggered || gasTriggered || doorLockTriggered)) {
      console.log('🔕 Uyarı koşulu yok:', { temp, motion, gas, doorLock });
      return null;
    }

    // Firestore belgesi
    const doc = {
      temperature:  temp,
      motion:       motion,
      gas:          gas,
      doorLock:     doorLock,
      triggered: {
        temperature: tempTriggered,
        motion:      motionTriggered,
        gas:         gasTriggered,
        doorLock:    doorLockTriggered
      },
      timestamp:    admin.firestore.FieldValue.serverTimestamp(),

    };

    console.log('📤 Firestore’a alert yazılıyor:', doc);
    return admin.firestore()
      .collection('sensorAlerts')
      .add(doc);
  }
);

/**
 * 2️⃣ Firestore’a yazılan her yeni sensorAlerts dökümanına
 * (örneğin gaz kaçağı olduğunda) bildirim gönder ve
 * doc.notified alanını true olarak güncelle
 */
exports.gasAlert = onValueUpdated(
  { ref: '/sensors/gas' },
  async (event) => {
    const before = event.data.before.toJSON();
    const after  = event.data.after.toJSON();
    console.log('💧 gasAlert before→after:', before, '→', after);

    // Gaz sensörü 0→1 geçişi
    if (after === true && before !== true) {
      const payload = {
        notification: {
          title: '🚨 Gaz Kaçağı Tespit Edildi!',
          body:  'Gaz seviyesi kritik: lütfen hemen kontrol edin.',
        },
        data: { alertType: 'gas' },
        topic: 'alerts',
      };
      console.log('📣 Gaz bildirimi gönderiliyor');
      return admin.messaging().send(payload);
    }

    console.log('🔕 Gaz koşulu yok.');
    return null;
  }
);
/**
 * 3️⃣ RealtimeDB’de /alarms/highTemperature yolundaki değişikliklere
 * bak, true olduktan sonra bildirim gönder
 */
exports.highTempAlert = onValueUpdated(
  { ref: '/alarms/highTemperature' },
  async (event) => {
    const before = event.data.before.toJSON();
    const after  = event.data.after.toJSON();
    console.log('🌡️ highTempAlert before→after:', before, after);

    if (after === true && before !== true) {
      const payload = {
        notification: {
          title: 'Yüksek Sıcaklık Uyarısı!',
          body:  'Ortam sıcaklığı tehlikeli seviyeye ulaştı.',
        },
        data: { alertType: 'highTemperature' },
        topic: 'alerts',
      };
      console.log('🔥 Yüksek sıcaklık bildirimi gönderiliyor');
      return admin.messaging().send(payload);
    }

    console.log('🔕 Yüksek sıcaklık koşulu yok.');
    return null;
  }
);
