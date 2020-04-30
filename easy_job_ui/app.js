var express = require('express');
var config = require("./src/config.json")
var app = express();
var bodyParser = require('body-parser');
app.use(bodyParser.urlencoded({extended : false}));
var superagent = require('superagent');

app.all('*', function(req, res, next) {
    res.header("Access-Control-Allow-Origin", "*");
    res.header("Access-Control-Allow-Headers", "Content-Type,Content-Length, Authorization, Accept,X-Requested-With");
    res.header("Access-Control-Allow-Methods","PUT,POST,GET,DELETE,OPTIONS");
    res.header("X-Powered-By",' 3.2.1')
    if(req.method=="OPTIONS") res.send(200);/*让options请求快速返回*/
    else  next();
});

function checkExistsIndex(dateList) {
    var promises=[];
  
    for(var date of dateList){
         promises.push(new Promise((res,reject)=>{
            superagent.head(config.api+date)
    
                    .then(
                        r=>{
                            res(true)
                        },
                        error=>{
                            res(false)
                        }
                    )
                    
         }))
    }

    return Promise.all(promises).then(data=>{
      var existDateList=[];
      for(var i=0;i<dateList.length;i++)
      {
        if(data[i]){
          existDateList.push(dateList[i])
        }
      }
      return existDateList
    })
   
  }

  app.post('/getInfo', function (req, res) {
    
    var str='';
    req.on("data",function(dt){
        str+=dt
    })

    checkExistsIndex(req.query.index.split(',')).then(existIndex=>{

        let url = config.es+existIndex+'/_search?from='+(req.query.from || 0)+'&size='+(req.query.size || 30);
        superagent
            .post(url)
            .set('Accept', 'application/json')
            .send(str)
            .timeout(20000)
            .end(function (err, response) {
                res.send(response.text);
            })
    })
});


app.use('/', express.static('dist'));

app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, 'dist/index.html'))
})


app.listen(config.port, function () {
  console.log('Server listening on port '+config.port+'!');
});