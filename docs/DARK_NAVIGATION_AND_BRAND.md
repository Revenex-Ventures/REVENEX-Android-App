# Dark navigation sizing and brand cleanup

The mobile-only changes replace the three independent dark bottom bars with one
shared presentation. Existing role destinations and navigation callbacks remain
unchanged; Administrator mobile access stays disabled.

Each item's minimum width comes from its rendered label at the active font size,
plus horizontal padding and the accessible target minimum. Available extra space
is shared proportionally. Longer labels such as Academics and Schedule therefore
fit inside their orange selection surface. When the whole row cannot fit, the
dock uses additional rows instead of truncating labels. Rounded-rectangle corners
replace the fully circular shape that cut into long labels. Icons use outline
variants, and selected tabs expose their actual selected semantics.

Teacher dark navigation now has the same navy container and orange filled
selection as Principal and Student. Light navigation remains on its existing
render path. Selection color transitions respect reduced motion. The outer app
shell continues to own gesture and keyboard insets.

The previous native vector artwork contained irregular stair-step photo-trace
contours. The RV and REVENEX assets have been rebuilt with clean lines and curves,
retaining the white/charcoal and gold identity and existing layout proportions.
The Android starting icon uses the same cleaned monogram. These are clean native
reconstructions based on the supplied company card, not a replacement claim for
an original editable corporate artwork master.

Verification covers every selected option for Principal, Teacher and Student at
360dp normal text and 320dp large text. Tests check navigation targets, selection,
48dp minimum touch height, text overflow and label bounds inside each surface.
Launch and routed portal renders are also refreshed. Actual build and render
results belong in the delivery report after checks finish.

No Supabase configuration, data, policies or payment behavior changes are needed.
