#/bin/bash
function export {
	inkscape --export-area-page --export-dpi $1 --export-png=$2 $3
	echo
}

if [ "$#" -ne 1 ]; then
	echo "Usage: $0 <svg file>"
	exit 0
fi

outdir="../src/main/res/mipmap"
dpi=90
outfile=ic_launcher.png


export $dpi $outdir-mdpi/$outfile $1
export $(echo "scale=2; $dpi*1.5" | bc) $outdir-hdpi/$outfile $1
export $(echo "scale=2; $dpi*2" | bc) $outdir-xhdpi/$outfile $1
export $(echo "scale=2; $dpi*3" | bc) $outdir-xxhdpi/$outfile $1
export $(echo "scale=2; $dpi*4" | bc) $outdir-xxxhdpi/$outfile $1
