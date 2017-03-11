FILE=$1

DENSITY[0]=160
DENSITY[1]=240
DENSITY[2]=320
DENSITY[3]=480
DENSITY[4]=640

FOLDERS[0]="../app/src/main/res/drawable-mdpi/"
FOLDERS[1]="../app/src/main/res/drawable-hdpi/"
FOLDERS[2]="../app/src/main/res/drawable-xhdpi/"
FOLDERS[3]="../app/src/main/res/drawable-xxhdpi/"
FOLDERS[4]="../app/src/main/res/drawable-xxxhdpi/"

NAME=`echo $FILE | awk '{ print substr( $0, 0, length($0)-4 ) }'`

convert $FILE[0] $NAME.png

for ((i=0;i<${#DENSITY[@]};++i)); do
  convert -units PixelsPerInch $NAME.png -resample "${DENSITY[i]}" "${FOLDERS[i]}"$NAME.png
done
rm $NAME.png

