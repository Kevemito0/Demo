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
    const snap = event.data.after;
    if (!snap.exists) {
      console.log('🚫 Veri yok, atlanıyor.');
      return null;
    }
    const data = snap.toJSON();
    const temp   = Number(data.temperature);
    const motion = Boolean(data.motion);
    const gas    = Number(data.gas);

    const kosul =
      temp   > 50    ||
      motion === true||
      gas    === 1;

    if (!kosul) {
      console.log('🔕 Uyarı koşulu yok:', { temp, motion, gas });
      return null;
    }

    const doc = {
      temperature: temp,
      motion:      motion,
      gas:         gas,
      timestamp:   admin.firestore.FieldValue.serverTimestamp(),
      notified:    false  // ileride bildirim gönderince true yapacağız
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
exports.sensorAlert = onDocumentUpdated(
  { document: 'sensorAlerts/{alertId}' },
  async (event) => {
    const before = event.data.before.data();
    const after  = event.data.after.data();
    console.log('🔄 sensorAlertNotification:', before, '→', after);

    // Gaz kaçağı: gas===1 ve henüz bildirim yollanmamışsa
    if (after.gas === 1 && before.notified === false) {
      const payload = {
        notification: {
          title: 'Gaz Kaçağı Tespit Edildi!',
          body:  'Lütfen acil müdahale edin.',
        },
        data: { alertType: 'gas' },
        topic: 'alerts',
      };
      console.log('📣 Gaz kaçağı bildirimi gönderiliyor');
      await admin.messaging().send(payload);
      // notified bayrağını güncelle
      return admin.firestore()
        .collection('sensorAlerts')
        .doc(event.data.after.id)
        .update({ notified: true });
    }

    console.log('🔕 Bildirim koşulu sağlanmadı.');
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
