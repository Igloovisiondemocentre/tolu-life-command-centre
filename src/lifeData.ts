export const financialSignals = [
  {label:"Settlement package",value:"£24,853 stated",state:"PENDING — NOT CASH",next:"Legal review, signature advice, cleared payment and payslip"},
  {label:"Take-home estimate",value:"~£25,800",state:"ESTIMATE ONLY",next:"Replace with actual net figures after payroll"},
  {label:"Cash-flow stability",value:"Improving",state:"FRAGILE",next:"Ring-fence a starter buffer; fund essentials first"},
  {label:"Debt position",value:"3 defaults + live plans",state:"HIGH PRIORITY",next:"Verify owners and written balances before paying"},
  {label:"Credit rebuild",value:"Repair phase",state:"PROTECT",next:"No missed live payments; verify electoral roll"}
];

export const settlementBuckets = [
  {name:"Uncertainty reserve",pct:10,color:"#a77bff",gate:"Tax, legal and payroll figures reconciled"},
  {name:"Essential runway",pct:25,color:"#52f7ff",gate:"Monthly essential budget calculated"},
  {name:"Verified debts",pct:35,color:"#ff5b45",gate:"Owners, balances and terms confirmed in writing"},
  {name:"Health & career",pct:15,color:"#ffb31f",gate:"Specific cost and outcome written down"},
  {name:"Long-term investing",pct:15,color:"#b9f227",gate:"Buffer built; priority debts controlled; 5+ year money"}
];

export const debts = [
  {name:"Perch / ACI / Lendable",amount:"~£2,550",priority:"CRITICAL",rule:"Treat as one account until ownership is confirmed."},
  {name:"Lowell / former Vanquis",amount:"£2,129",priority:"HIGH",rule:"Count once; request written settlement terms."},
  {name:"Lantern Debt Recovery",amount:"£672",priority:"HIGH",rule:"Use only the latest verified balance."},
  {name:"O2 device loan",amount:"£494 / £26 monthly",priority:"PROTECT",rule:"Keep funded to protect positive history."},
  {name:"Nationwide marker",amount:"£0 balance",priority:"REPAIR",rule:"Track arrangement marker, not as debt."}
];

export const spendingPatterns = [
  ["Convenience food","Frequent small purchases","Two planned shops + weekly food pot"],
  ["Eating out & delivery","High avoidable frequency","Two planned paid meals maximum"],
  ["Transport","TFL, Lime and Uber overlap","Resolve unpaid fares; choose default mode"],
  ["Cash withdrawals","Low traceability + fees","One recorded withdrawal per week"],
  ["Transfers","11 Revolut top-ups in a month","One planned weekly transfer"],
  ["Subscriptions","Several recurring services","Quarterly keep / cancel / downgrade audit"]
];

export const recentLifeEvents = [
  {id:"TL-015",date:"12 Jul",method:"SECURITY",title:"Equifax activity requires confirmation",task:"CC-012"},
  {id:"TL-014",date:"11 Jul",method:"SECURITY",title:"OpenAI authentication settings changed",task:"CC-012"},
  {id:"TL-012",date:"12 Jul",method:"WHATSAPP",title:"Keys, belongings and storage discussed with Toro",task:"CC-011"},
  {id:"TL-011",date:"12 Jul",method:"WHATSAPP",title:"Immediate housing plan discussed with Mum",task:"CC-011"},
  {id:"TL-010",date:"11 Jul",method:"EMAIL",title:"Clearpay investigation still open",task:"CC-007"},
  {id:"TL-009",date:"10 Jul",method:"EMAIL",title:"ACI collection and home-visit notice",task:"CC-003"}
];

export const nonCashAssets = [
  ["Rolex","£10k–£15k","Authenticate, photograph, insure"],
  ["Rado","~£2,500","Document model, condition and ownership"],
  ["Alienware laptop","~£2,000","Record serial, back up and review cover"],
  ["Phone","~£1,500","Record IMEI, backup and insurance"]
];
