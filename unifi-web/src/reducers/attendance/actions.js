import { WSPackage } from '../../lib/ws'

import { LIST_SCHEDULE_STATS, LIST_BLOCKS, GET_CONTACT_ATTENDANCE_FOR_SCHEDULE } from './types'

export function listScheduleStats() {

  const pack = new WSPackage({
    protocolVersion: '1.0.0',
    releaseVersion:  '1.0.0',
    messageType:     'attendance.schedule.list-schedule-stats',
    payload:         { clientId: 'ucl-mgmt' }
  });

  return {
    type: LIST_SCHEDULE_STATS,
    socketRequest: pack.content
  };

}

export function listBlocks(scheduleId) {

  const pack = new WSPackage({
    protocolVersion: '1.0.0',
    releaseVersion:  '1.0.0',
    messageType:     'attendance.schedule.list-blocks',
    payload:         {
      clientId:   'ucl-mgmt',
      scheduleId
    }
  });

  return {
    type: LIST_BLOCKS,
    socketRequest: pack.content
  };

}

export function getContactAttendanceForSchedule(scheduleId) {

  const pack = new WSPackage({
    protocolVersion: '1.0.0',
    releaseVersion:  '1.0.0',
    messageType:     'attendance.schedule.get-contact-attendance-for-schedule',
    payload:         {
      clientId:   'ucl-mgmt',
      scheduleId
    }
  });

  return {
    type: GET_CONTACT_ATTENDANCE_FOR_SCHEDULE,
    socketRequest: pack.content
  };

}
export default null
