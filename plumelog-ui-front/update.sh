echo '--start build--'
npm run build

echo '-delete resources file-'
rm -rf ../plumelog_ui/src/main/resources/templates/*
rm -rf ../plumelog_ui/src/main/resources/static/*

echo '--move file to ui resource--'
mv ./dist/index.html ../plumelog_ui/src/main/resources/templates/
mv ./dist/* ../plumelog_ui/src/main/resources/static
