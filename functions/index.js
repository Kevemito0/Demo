// functions/index.js
const admin = require('firebase-admin');
const { setGlobalOptions } = require('firebase-functions');
const {
  onValueWritten,
  onValueUpdated
} = require('firebase-functions/v2/database');

admin.initializeApp();
setGlobalOptions({ region: 'us-central1' });

/**
 * 1️⃣ RealtimeDB /sensors → Firestore alerts
 */
exports.syncSensorData = onValueWritten(
  { ref: '/sensors' },
  async (event) => {
    const beforeSnap = event.data.before;
    const afterSnap  = event.data.after;

    const before = beforeSnap.exists() ? beforeSnap.val() : {};
    const after  = afterSnap.exists()  ? afterSnap.val()  : {};

    const prevTemp     = Number(before.temperature  || 0);
    const temp         = Number(after.temperature   || 0);

    const prevMotion   = Boolean(before.motion);
    const motion       = Boolean(after.motion);

    const prevGas      = Boolean(before.gas);
    const gas          = Boolean(after.gas);

    const prevDoorLock = Boolean(before.doorLock);
    const doorLock     = Boolean(after.doorLock);

    // transitions: ≤50→>50, false→true, false→true, false→true
    const tempTriggered     = prevTemp < 50      && temp >= 50;
    const motionTriggered   = prevMotion !== true && motion === true;
    const gasTriggered      = prevGas !== true   && gas   === true;
    const doorLockTriggered = !prevDoorLock      && doorLock;

    if (!(tempTriggered || motionTriggered || gasTriggered || doorLockTriggered)) {
      console.log('🔕 No alert conditions met:', { temp, motion, gas, doorLock });
      return null;
    }

    const doc = {
      temperature: temp,
      motion,
      gas,
      doorLock,
      triggered: {
        temperature: tempTriggered,
        motion: motionTriggered,
        gas:    gasTriggered,
        doorLock: doorLockTriggered
      },
      timestamp: admin.firestore.FieldValue.serverTimestamp()
    };

    console.log('📤 Writing alert to Firestore:', doc);
    return admin.firestore()
      .collection('sensorAlerts')
      .add(doc);
  }
);

/**
 * 2️⃣ Gas sensor 0→1
 */
exports.gasAlert = onValueUpdated(
  { ref: '/sensors/gas' },
  async (event) => {
    const before = event.data.before.toJSON();
    const after  = event.data.after.toJSON();
    console.log('💧 gasAlert before→after:', before, '→', after);

    if (after === true && before !== true) {
      const payload = {
        notification: {
          title: 'Gas Leak Detected!',
          body:  'Critical gas levels detected. Please check immediately!',
        },
        data: { alertType: 'gas' },
        topic: 'alerts',
      };
      console.log('📣 Sending gas leak notification');
      return admin.messaging().send(payload);
    }

    console.log('🔕 No gas alert');
    return null;
  }
);

/**
 * 3️⃣ Motion sensor false→true → new “motionAlert”
 */
exports.motionAlert = onValueUpdated(
  { ref: '/sensors/motion' },
  async (event) => {
    const before = event.data.before.toJSON();
    const after  = event.data.after.toJSON();
    console.log('🕵️ motionAlert before→after:', before, '→', after);

    if (after === true && before !== true) {
      const payload = {
        notification: {
          title: 'Motion Detected!',
          body:  'Movement has been detected. Please verify the area.',
        },
        data: { alertType: 'motion' },
        topic: 'alerts',
      };
      console.log('📣 Sending motion detection notification');
      return admin.messaging().send(payload);
    }

    console.log('🔕 No motion alert');
    return null;
  }
);

/**
 * 4️⃣ High‐temperature alarm → English notification
 */
exports.highTempAlert = onValueUpdated(
  { ref: '/alarms/highTemperature' },
  async (event) => {
    const before = event.data.before.toJSON();
    const after  = event.data.after.toJSON();
    console.log('🌡️ highTempAlert before→after:', before, '→', after);

    if (after === true && before !== true) {
      const payload = {
        notification: {
          title: 'High Temperature Alert!',
          body:  'Ambient temperature has reached dangerous levels.',
        },
        data: { alertType: 'highTemperature' },
        topic: 'alerts',
      };
      console.log('🔥 Sending high‐temperature notification');
      return admin.messaging().send(payload);
    }

    console.log('🔕 No high‐temperature alert');
    return null;
  }
);
