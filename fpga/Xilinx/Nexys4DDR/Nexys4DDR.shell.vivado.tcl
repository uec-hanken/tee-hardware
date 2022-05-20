# Copied from results got from the freedom package. See LICENSE for details.
# Get the TCL location
set shell_vivado_tcl [file normalize [info script]]
# Now, get the folder where is located
set shell_vivado_dir [file dirname $shell_vivado_tcl]
# Get the folder and the base-name (maybe useful afterwards)
set shell_vivado_idx [string last ".shell.vivado.tcl" $shell_vivado_tcl]
# Now, add those xdc files here. This allows all the xdc files to be added as long as they are in the same path as this tcl.
add_files -quiet -norecurse -fileset [current_fileset -constrset] [lsort [glob -directory $shell_vivado_dir -nocomplain {*.xdc}]]

