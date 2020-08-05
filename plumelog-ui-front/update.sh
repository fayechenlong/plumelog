echo '--start build--'
npm run build

echo '-delete resources file-'
rm -rf ../plumelog-server/src/main/resources/templates/*
rm -rf ../plumelog-server/src/main/resources/static/*

echo '--move file to ui resource--'
mv ./dist/index.html ../plumelog-server/src/main/resources/templates/
mv ./dist/* ../plumelog-server/src/main/resources/static
