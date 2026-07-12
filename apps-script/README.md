# Command Centre write bridge

This optional Google Apps Script turns the app's Source Bridge into direct write-back.

1. Open the Personal Command Centre Sheet and choose Extensions → Apps Script.
2. Replace `Code.gs` with this folder's `Code.gs`.
3. In Project Settings → Script properties, add `COMMAND_SECRET` with a long private value.
4. Deploy → New deployment → Web app. Execute as yourself. Allow access to anyone with the deployment URL.
5. Paste the `/exec` URL and the same secret into the app's Source Bridge.

The endpoint writes only validated values into columns B:H of the first empty Task Controls row. It does not edit Master Tracker, Change Log or Task Timeline directly; the existing scheduled automation remains responsible for those audited changes.
