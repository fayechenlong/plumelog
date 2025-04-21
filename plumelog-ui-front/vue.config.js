const path = require('path')
const fs = require('fs-extra')

module.exports = {
  productionSourceMap: false,
  publicPath: './',
  configureWebpack: {
    devtool: "source-map",
    plugins: [
      {
        apply(compiler) {
          compiler.hooks.afterEmit.tap('CopyResourcesPlugin', () => {
            fs.removeSync(path.resolve(__dirname, '../plumelog-server/src/main/resources/templates'))
            fs.removeSync(path.resolve(__dirname, '../plumelog-server/src/main/resources/static'))
            fs.removeSync(path.resolve(__dirname, '../plumelog-lite/src/main/resources/templates'))
            fs.removeSync(path.resolve(__dirname, '../plumelog-lite/src/main/resources/META-INF/resources/plumelog'))

            fs.copySync(
              path.resolve(__dirname, 'dist/index.html'),
              path.resolve(__dirname, '../plumelog-server/src/main/resources/templates/plumelog.html')
            )
            fs.copySync(
              path.resolve(__dirname, 'dist'),
              path.resolve(__dirname, '../plumelog-server/src/main/resources/static')
            )
            fs.copySync(
              path.resolve(__dirname, 'dist/index.html'),
              path.resolve(__dirname, '../plumelog-lite/src/main/resources/templates/plumelog.html')
            )
            fs.copySync(
              path.resolve(__dirname, 'dist'),
              path.resolve(__dirname, '../plumelog-lite/src/main/resources/META-INF/resources/plumelog')
            )
          })
        }
      }
    ]
  }
}