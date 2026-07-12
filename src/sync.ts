import { unzipSync, strFromU8 } from "fflate";
import type { Mission } from "./data";

export type TimelineEntry={id:string;taskId:string;date:string;type:string;method:string;direction:string;person:string;summary:string;details:string;status:string;next:string;sourceUrl?:string};
export type SyncModel={missions:Mission[];timeline:TimelineEntry[];calendar:string[][];financialHealth:string[][];financeDebts:string[][];contacts:string[][];syncedAt:string;sourceName:string};

export const seedTimeline:TimelineEntry[]=[
  ["TL-001","CC-008","19 Jun 2026 09:00","Notice received","Email","Incoming","Isabell Edwards-Adamson | Greenhalgh Kerr","Possession hearing correspondence received","Hearing and rent-liability correspondence received; venue still requires confirmation.","In progress","Prepare evidence and confirm venue before 2 Sep 2026.","https://mail.google.com/mail/#all/19edf2b113536cb6"],
  ["TL-002","CC-009","06 Jul 2026 09:00","Claim received","Email","Incoming","Lerayne Nisbett | Folio London","July rent-arrears claim received","Urgent payment request received; figures need checking against payment evidence.","Needs review","Review records and respond by 13 Jul 2026.","https://mail.google.com/mail/#all/19f377d18ee1e5aa"],
  ["TL-003","CC-002","09 Jul 2026 09:00","Agreement update","Email","Incoming","Niamh Kelly | The HR Dept","Settlement draft and deadline update","Revised agreement and employer position recorded.","In progress","Complete legal review before deadline.","https://mail.google.com/mail/#all/19f3e0b698658096"],
  ["TL-004","CC-004","09 Jul 2026 09:00","Appeal sent","Email","Outgoing / thread","Gary Walker | St George's","Physiotherapy appeal and surgery escalation","Appeal and worsening symptoms explained; response pending.","Waiting","Follow up if no response by 17 Jul.","https://mail.google.com/mail/#all/19f45ee8a21578c5"],
  ["TL-005","CC-005","09 Jul 2026 10:00","Complaint decision","Email","Incoming","Nationwide Complaints Team","Compensation confirmed","Decision received with compensation and Ombudsman rights.","Waiting","Check payment on 20 Jul.","https://mail.google.com/mail/#all/19f476b83b08f4a8"],
  ["TL-006","CC-010","09 Jul 2026 10:00","Referral right confirmed","Email","Incoming","Nationwide Complaints Team","Ombudsman window opened","Six-month referral period recorded.","Not started","Use only if unresolved by 9 Jan 2027.","https://mail.google.com/mail/#all/19f476b83b08f4a8"],
  ["TL-007","CC-006","09 Jul 2026 11:00","Billing update","Email","Incoming","O2 Billing & Collections","Payment-plan information received","Current bill and plan still need verification.","In progress","Verify before 31 Jul.","https://mail.google.com/mail/#all/19f3c183bbc678e3"],
  ["TL-008","CC-001","10 Jul 2026 09:00","Consultation coordination","Email","Incoming / thread","Lincs Law","Settlement consultation arranged","Documents, identification and latest agreement requested.","In progress","Prepare documents before consultation.","https://mail.google.com/mail/#all/19f4b4b8efffd6bf"],
  ["TL-009","CC-003","10 Jul 2026 10:00","Collection notice","Email","Incoming","ACI UK / Verify","Home-visit and arrears notice received","Affordable written arrangement needed.","Not started","Contact ACI.","https://mail.google.com/mail/#all/19f4d00cd1d71402"],
  ["TL-010","CC-007","11 Jul 2026 16:01","Complaint update","Email","Incoming","Clearpay","Investigation still open","No outcome yet.","Waiting","Chase by 18 Aug.","https://mail.google.com/mail/#all/19f51e96861b227e"],
  ["TL-011","CC-011","12 Jul 2026 06:10","Housing discussion","WhatsApp","Two-way","Mum","Next place and move discussed","Immediate housing destination still needs a decision.","Needs review","Confirm plan by 17 Jul.","https://drive.google.com/file/d/1NiCi6tMNAFzYH59ATB7f1nu_rUWQZzNV/view"],
  ["TL-012","CC-011","12 Jul 2026 06:09","Moving support","WhatsApp","Two-way","Toro","Keys, belongings and storage discussed","Practical support around access and moving.","Needs review","Confirm storage and keys.","https://drive.google.com/file/d/1LnQkpsfW76i3lgCG5bygujOKvCVF6L7y/view"],
  ["TL-013","CC-012","11 Jul 2026 20:28","Account access alert","Email","Incoming","Google and Yahoo","Email migration access recorded","Migration and sign-in notifications received.","Needs review","Confirm activity was authorised.","https://mail.google.com/mail/#all/19f52de7776853db"],
  ["TL-014","CC-012","11 Jul 2026 18:06","Account settings alert","Email","Incoming","OpenAI","Authentication settings changed","Security settings notification received.","Needs review","Confirm change was expected.","https://mail.google.com/mail/#all/19f525c20ca43f91"],
  ["TL-015","CC-012","12 Jul 2026 07:02","Account activity alert","Email","Incoming","Equifax","Recent account activity recorded","Two access notifications arrived close together.","Needs review","Confirm both actions were yours.","https://mail.google.com/mail/#all/19f54ec7edafcd8b"]
].map(([id,taskId,date,type,method,direction,person,summary,details,status,next,sourceUrl])=>({id,taskId,date,type,method,direction,person,summary,details,status,next,sourceUrl}));

const text=(xml:string)=>new DOMParser().parseFromString(xml,"application/xml");
const excelDate=(n:number)=>{const d=new Date(Math.round((n-25569)*86400*1000));return d.toLocaleDateString("en-GB",{day:"numeric",month:"short",year:"numeric",timeZone:"UTC"})};
const cellCol=(ref:string)=>{let n=0;for(const c of ref.match(/[A-Z]+/)?.[0]||"")n=n*26+c.charCodeAt(0)-64;return n-1};

function workbookSheets(files:Record<string,Uint8Array>){
  const wb=text(strFromU8(files["xl/workbook.xml"]));
  const rel=text(strFromU8(files["xl/_rels/workbook.xml.rels"]));
  const targets=new Map([...rel.querySelectorAll("Relationship")].map(x=>[x.getAttribute("Id"),x.getAttribute("Target")||""]));
  return [...wb.querySelectorAll("sheet")].map(s=>({name:s.getAttribute("name")||"",path:`xl/${targets.get(s.getAttribute("r:id"))}`.replace("xl//","xl/")}));
}

function readRows(files:Record<string,Uint8Array>,path:string,shared:string[]){
  const doc=text(strFromU8(files[path]));const rows:string[][]=[];
  for(const row of doc.querySelectorAll("row")){const out:string[]=[];for(const c of row.querySelectorAll("c")){const col=cellCol(c.getAttribute("r")||"");const type=c.getAttribute("t");const raw=c.querySelector("v")?.textContent||c.querySelector("is t")?.textContent||"";const formula=c.querySelector("f")?.textContent||"";let value=type==="s"?shared[Number(raw)]||"":raw;if(formula.startsWith("HYPERLINK(")){const m=formula.match(/HYPERLINK\("([^"]+)/);if(m)value=m[1]}out[col]=value}rows.push(out)}return rows;
}
function objects(rows:string[][]){const heads=rows[0]||[];return rows.slice(1).filter(r=>r.some(Boolean)).map(r=>Object.fromEntries(heads.map((h,i)=>[h,r[i]??""])))}

export async function parseCommandCentre(file:File):Promise<SyncModel>{
  const files=unzipSync(new Uint8Array(await file.arrayBuffer()));
  const sharedFile=files["xl/sharedStrings.xml"];
  const shared=sharedFile?[...text(strFromU8(sharedFile)).querySelectorAll("si")].map(si=>[...si.querySelectorAll("t")].map(t=>t.textContent||"").join("")):[];
  const sheets=new Map(workbookSheets(files).map(s=>[s.name,readRows(files,s.path,shared)]));
  const tracker=objects(sheets.get("Master Tracker")||[]);
  const validPriority=(p:string):Mission["priority"]=>["Critical","High","Medium","Low"].includes(p)?p as Mission["priority"]:"Medium";
  const missions=tracker.filter(r=>r.ID).map(r=>({id:r.ID,category:r.Category,title:r.Item,status:r.Status,priority:validPriority(r.Priority),next:r["Next Action"],due:Number(r["Due Date"])?excelDate(Number(r["Due Date"])):r["Due Date"],days:Number(r["Days Left"]||0),waiting:r["Waiting On"],notes:r.Notes,conversation:r["Conversation With"],context:r["Conversation Context"],sourceUrl:r["Source URL"]||r["Gmail Thread"],timelineEvents:Number(r["Timeline Events"]||0)}));
  const timeline=objects(sheets.get("Task Timeline")||[]).filter(r=>r["Event ID"]).map(r=>({id:r["Event ID"],taskId:r["Task ID"],date:Number(r["Event Date / Time"])?excelDate(Number(r["Event Date / Time"])):r["Event Date / Time"],type:r["Event Type"],method:r["Communication Method"],direction:r.Direction,person:r["Person / Organisation"],summary:r.Summary,details:r["Details / Outcome"],status:r["Status After"],next:r["Next Action / Due"],sourceUrl:r["Source Link"]}));
  return {missions,timeline,calendar:sheets.get("Calendar & Deadlines")||[],financialHealth:sheets.get("Financial Health")||[],financeDebts:sheets.get("Finance & Debts")||[],contacts:sheets.get("Contacts & Threads")||[],syncedAt:new Date().toISOString(),sourceName:file.name};
}
