echo '--start build--'
npm run build

echo '-delete resources file-'
rm -rf ../plumelog-ui/src/main/resources/templates/*
rm -rf ../plumelog-ui/src/main/resources/static/*

echo '--move file to ui resource--'
mv ./dist/index.html ../plumelog-ui/src/main/resources/templates/
mv ./dist/* ../plumelog-ui/src/main/resources/static
