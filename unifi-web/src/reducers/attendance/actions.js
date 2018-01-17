import { WSPackage } from '../../lib/ws'
import { clientId } from "../../index";

import {
  LIST_SCHEDULE_STATS,
  LIST_BLOCKS,
  GET_CONTACT_ATTENDANCE_FOR_SCHEDULE,
  REPORT_BLOCK_ATTENDANCE,
  REPORT_CONTACT_SCHEDULE_ATTENDANCE,
  OVERRIDE_ATTENDANCE
} from './types'

export function listScheduleStats() {

  const pack = new WSPackage({
    protocolVersion: '1.0.0',
    releaseVersion:  '1.0.0',
    messageType:     'attendance.schedule.list-schedule-stats',
    payload:         { clientId }
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
    payload:         { clientId, scheduleId }
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
    payload:         { clientId, scheduleId }
  });

  return {
    type: GET_CONTACT_ATTENDANCE_FOR_SCHEDULE,
    socketRequest: pack.content
  };

}

export function reportBlockAttendance(scheduleId, clientReference) {

  const pack = new WSPackage({
    protocolVersion: '1.0.0',
    releaseVersion:  '1.0.0',
    messageType:     'attendance.schedule.report-block-attendance',
    payload:         { clientId, scheduleId, clientReference }
  });

  return {
    type: REPORT_BLOCK_ATTENDANCE,
    socketRequest: pack.content
  };

}

export function reportContactScheduleAttendance() {

  const pack = new WSPackage({
    protocolVersion: '1.0.0',
    releaseVersion:  '1.0.0',
    messageType:     'attendance.schedule.report-contact-schedule-attendance',
    payload:         { clientId }
  });

  return {
    type: REPORT_CONTACT_SCHEDULE_ATTENDANCE,
    socketRequest: pack.content
  };

}

export function overrideAttendance(clientReference, scheduleId, blockId, status) {

  const pack = new WSPackage({
    protocolVersion: '1.0.0',
    releaseVersion:  '1.0.0',
    messageType:     'attendance.schedule.override-attendance',
    payload:         { clientId, clientReference, scheduleId, blockId, status }
  });

  return {
    type: OVERRIDE_ATTENDANCE,
    socketRequest: pack.content
  };

}

export default null
