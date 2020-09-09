import moment from 'moment'
export default {
    shortcuts: [
        {
             text: '15分钟内',
             value () {
                 const end = moment(moment(new Date().getTime()).format('YYYY-MM-DD 23:59:59'));
                 const start = new Date();
                 start.setTime(start.getTime() - 60 * 1000 * 15);
                 return [start, end];
             }
         },
         {
             text: '30分钟内',
             value () {
                 const end = moment(moment(new Date().getTime()).format('YYYY-MM-DD 23:59:59'));
                 const start = new Date();
                 start.setTime(start.getTime() - 60 * 1000 * 30);
                 return [start, end];
             }
         },
         {
             text: '1小时内',
             value () {
                 const end = moment(moment(new Date().getTime()).format('YYYY-MM-DD 23:59:59'));
                 const start = new Date();
                 start.setTime(start.getTime() - 3600 * 1000);
                 return [start, end];
             }
         },
         {
             text: '24小时内',
             value () {
                 const end = moment(moment(new Date().getTime()).format('YYYY-MM-DD 23:59:59'));
                 const start = new Date();
                 start.setTime(start.getTime() - 3600 * 1000 * 24);
                 return [start, end];
             }
         },
         {
             text: '1周内',
             value () {
                 const end = moment(moment(new Date().getTime()).format('YYYY-MM-DD 23:59:59'));
                 const start = new Date();
                 start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
                 return [start, end];
             }
         },
         {
             text: '当天',
             value () {
                 const end = moment(moment(new Date().getTime()).format('YYYY-MM-DD 23:59:59'));
                 var start = new Date();
                 start.setTime(start.setHours(0,0));
                 return [start, end];
             }
         }
     ],
      disabledDate(date){
        return date && date.valueOf() > Date.now();
      }
}