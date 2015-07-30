#!/bin/bash


# get args
if [ "$#" -ne 1 ]; then
	echo "Usage: $0 <post url>"
	exit 0
fi

# download post site
content=$(wget $1 -q -O -)

# parse site
reading_newsletter=false
while read -r line; do
	if [[ $line == *"<!-- Start Newsletter -->"* ]]
	then
		reading_newsletter=true
		continue
	fi
	if [[ $line == *"<!-- End Newsletter -->"* ]]
	then
		reading_newsletter=false
	fi
	if [ $reading_newsletter = true ]
	then
		echo $line
	fi
done <<< "$content"
