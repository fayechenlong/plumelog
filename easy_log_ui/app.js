var express = require('express');
var config = require("./src/config.json")
var app = express();
var bodyParser = require('body-parser');
app.use(bodyParser.urlencoded({extended : false}));
var superagent = require('superagent');
const path = require('path');

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

function formartTrace(list){


  //todo:检测数据是否闭合（判断<和>的数量是否一致）

  let zIndex=0;
  let _list = [];

  function pushItem(item,isStart){

    // console.log("zIndex:"+zIndex);

    let _arrary=_list;
    //找到该层级的最后一个元素往里插
    if(zIndex>-1){
      
      for(var i=0;i<zIndex;i++){
          _arrary = _arrary[_arrary.length-1].children;
      }

     
      //方法开始
      if(isStart){
        _arrary.push({
          method: item.method,
          appName: item.appName,
          start_time: item.time,
          children:[]
        });
        // console.log('start');
        // console.log('zindex:'+zIndex);
        // console.log(JSON.stringify(_arrary,null,2));
      }
      //方法结束
      else
      {
        //找到一个没结束的item
        for(var f=0;f<_arrary.length;f++){
          if(!_arrary[f].end_time){
            _arrary[f].end_time = item.time;
            break
          }
        }
        
        // console.log('close');
        // console.log('zindex:'+zIndex);
        // console.log(JSON.stringify(_arrary,null,2));
        // _arrary.end_time = item.time;
        // console.log(JSON.stringify(_list,null,2));
        // console.log('========================');
        // console.log(JSON.stringify(_arrary,null,2));
      }
      
      // console.log('zindex:'+zIndex);
      // console.log(JSON.stringify(_list));
    }
    else
    {
      _arrary.push({
        method: item.method,
        appName: item.appName,
        start_time: item.time,
        children:[]
      })
    }
  }

  for(var i=0;i<list.length;i++){
    //如果postion是 '<' 说明是上一个方法的子方法
    if(list[i]['position']=='<'){
      pushItem(list[i],true)
      zIndex++;

    }
    else if(list[i]['position']=='>')
    {
      zIndex--;
      pushItem(list[i],false)
    }
  }

  // console.log(JSON.stringify(_list,null,2))
  return _list;
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

app.get('/getTrace', function (req, res) {

  //console.log('traceId:'+req.query.traceId)
  if(req.query.traceId){
    checkExistsIndex(req.query.index.split(',')).then(existIndex=>{
        let filter = {
          "query": {
            "bool": {
              "must": [{
                "match": {
                  "traceId": {
                    "query": req.query.traceId
                  }
                }
              }]
            }
          },
          "sort": [{
            "positionNum": "asc"
          }]
        };

        let url = config.es+existIndex+'/_search';
        superagent
            .post(url)
            .set('Accept', 'application/json')
            .send(JSON.stringify(filter))
            .timeout(20000)
            .end(function (err, response) {
                let hits = [];
                
                try{
                  var result = JSON.parse(response.text);
                  result.hits.hits.map(hit=>{
                    hits.push(hit._source)
                  })
                }
                catch(e){
                  console.log('get hits error,'+e.message)
                }

                if(hits.length>0)
                {
                  res.send(formartTrace(hits));
                }
                else
                {
                  res.send({});
                }

            })
    })
  }
  else
  {
    res.send('{}');
  }
    
 
});


app.use('/', express.static('dist'));

app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, 'dist/index.html'))
})


app.listen(config.port, function () {
  console.log('Server listening on port '+config.port+'!');
});