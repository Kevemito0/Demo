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


    if (!kosul) {
      console.log('🔕 Uyarı koşulu yok:', { temp, motion, gas });
      return null;
    }


      const payload = {
        notification: {
      };
    }

    return null;
