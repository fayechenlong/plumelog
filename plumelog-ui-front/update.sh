echo '--start build--'
npm run build

echo '-delete resources file-'
rm -rf ../plumelog-server/src/main/resources/templates/*
rm -rf ../plumelog-server/src/main/resources/static/*

rm -rf ../plumelog-lite/src/main/resources/templates/*
rm -rf ../plumelog-lite/src/main/resources/static/*

echo '--copy file to ui resource--'
cp ./dist/index.html ../plumelog-server/src/main/resources/templates/plumelog.html
cp -R ./dist/. ../plumelog-server/src/main/resources/static

cp ./dist/index.html ../plumelog-lite/src/main/resources/templates/plumelog.html
cp -R ./dist/. ../plumelog-lite/src/main/resources/static/plumelog
