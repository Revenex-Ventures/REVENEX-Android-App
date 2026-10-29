# Principal dark presentation — implementation checklist

Student delivery checkpoint: `c5de86ecb327984616ee841930db4733a029a2af`.
Principal work follows that checkpoint and is still under visual review.

The two supplied Principal boards define the structure. The official Revenex
artwork is retained. Principal-only semantic tokens use canvas `#0B1220`, cards
`#1C2B43`, navigation `#162237`, orange `#FF743D`, blue `#92BAF2`, mint `#A6D9BF`.
Light paths and other role palettes are unchanged.

| Screen | Current implementation | Verification still needed |
| --- | --- | --- |
| Home | Distinct dark composition: attendance feature, student/staff metrics, modules, review counts, collection scope, notice | Runtime navigation and lower scroll capture |
| Students | Count summary, visible search, filters, dark record rows | Reference comparison and admission dialog |
| Staff | Teaching count, visible search, department filter, allocation route | Reference comparison and detail |
| Attendance | Saved-date/slot selectors, summaries and class histories | Dark contrast refinement and read-only history |
| Academics | Academic-year feature, six modules, real review state | Nested result review and publication lock |
| Fees | Separate collected/outstanding summaries and invoice/receipt rows | Dialog and receipt routing |
| Reports | Shared dark module grid, scoped real tables, existing CSV export | Export picker/device check |
| Approvals | Filtered pending/reviewed requests, dark records | Decision confirmation/error dialog |
| Leave review | Mint applicant summary, existing authorized decision flow | Runtime decision states |
| Account | Existing identity, settings, persisted appearance and logout | Dark composition refinement |
| Secondary | Subjects, calendar, allocations inherit shared rows/tokens | Full route audit, inbox/notices/details |

The first Principal verification run passed assembleDebug, selected Principal
render/approval/export tests, Student dark regression tests and lint. This is
local evidence, not hosted Supabase or a real Razorpay transaction. Further
refinement and fresh verification are required before calling the portal done.

Collections are labeled as the loaded session ledger; the source does not
establish a month-scoped total. Missing school display names are shown honestly.
No database migrations, permissions, payment logic or private configuration were
changed by this presentation work. Teacher dark work remains queued until this
portal is complete.
