const COMMAND_SHEET_ID = '1DxuXtZEsYsNBSgsaP767nw10NcqLTLDePofo1GOBT_Q';
const CONTROL_TAB = 'Task Controls';

function output_(payload) {
  return ContentService.createTextOutput(JSON.stringify(payload))
    .setMimeType(ContentService.MimeType.JSON);
}

function doGet() {
  return output_({ ok: true, service: 'Tolu Command Centre write bridge' });
}

function doPost(e) {
  const lock = LockService.getScriptLock();
  try {
    lock.waitLock(10000);
    const payload = JSON.parse((e.postData && e.postData.contents) || '{}');
    const expected = PropertiesService.getScriptProperties().getProperty('COMMAND_SECRET');
    if (!expected || payload.secret !== expected) {
      return output_({ ok: false, error: 'Unauthorised' });
    }

    const allowedActions = ['Add', 'Update', 'Complete', 'Reopen', 'Archive / remove', 'Restore', 'Cancel', 'Save plan', 'Log progress', 'Log check-in', 'Log win'];
    const allowedFields = ['Entire item', 'Item title', 'Category', 'Status', 'Priority', 'Next Action', 'Due Date', 'Waiting On', 'Notes', 'Conversation With', 'Conversation Context', 'Gmail Thread', 'Finance amount/status', 'Calendar date/time', 'Planner progress', 'Check-in', 'Win'];
    if (!payload.requestId || !payload.target || !allowedActions.includes(payload.action) || !allowedFields.includes(payload.field)) {
      return output_({ ok: false, error: 'Invalid Task Controls request' });
    }

    const sheet = SpreadsheetApp.openById(COMMAND_SHEET_ID).getSheetByName(CONTROL_TAB);
    const requestIds = sheet.getRange(6, 1, sheet.getMaxRows() - 5, 1).getDisplayValues();
    const duplicate = requestIds.findIndex(row => row[0] === String(payload.requestId));
    if (duplicate >= 0) return output_({ ok: true, row: 6 + duplicate, requestId: String(payload.requestId), duplicate: true });
    const emptyOffset = requestIds.findIndex(row => !row[0]);
    if (emptyOffset < 0) return output_({ ok: false, error: 'Task Controls queue is full' });
    const row = 6 + emptyOffset;

    sheet.getRange(row, 1, 1, 8).setValues([[
      String(payload.requestId),
      String(payload.target),
      String(payload.action),
      String(payload.field),
      String(payload.details || ''),
      String(payload.submittedBy || 'Tolu Command Centre App'),
      new Date(payload.requestedOn || Date.now()),
      'Yes'
    ]]);
    SpreadsheetApp.flush();
    return output_({ ok: true, row: row, requestId: String(payload.requestId) });
  } catch (error) {
    return output_({ ok: false, error: String(error && error.message || error) });
  } finally {
    lock.releaseLock();
  }
}
