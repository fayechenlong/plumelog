<style lang="less" scoped>
   
    .detail_table
    {
        td{
            background:none;
        }
    }
</style>
<template>
    <div>
        <Row class="expand-row">
            <Col span="24">
                <span class="expand-value">
                    <table class="detail_table">
                        <tr>
                          <td class="key">类名</td>
                          <td class="value">{{row.className}}</td>
                        </tr>
                        <tr>
                          <td class="key">方法名</td>
                          <td class="value">{{row.method}}</td>
                        </tr>
                        <tr>
                          <td class="key">内容</td>
                          <td class="value" v-html="hightLightCode(row.highlightCnt ||row.content)">
                          </td>
                        </tr>
                      </table>
                </span>
            </Col>
        </Row>
    </div>
</template>
<script>
    export default {
        props: {
            searchKey: String,
            row: Object,
        },
        methods:{
            hightLightCode(code){
                code = code.replace(/\\n\\t/g,"\n").replace(/\\n\\tat/g,"\n").replace(/\\n/g, '\n');
                if(code.indexOf('java.')>-1){
                    return '<pre style="word-break:break-all;white-space: normal;">'+Prism.highlight(code.replace(/\n/g,'<br/>'), Prism.languages.stackjava, 'stackjava').replace(/&lt;/g,'<').replace(/&gt;/g,'>')+"</pre>"
                }
                else if(code.indexOf("xml") > 0) {
                   return '<xmp>' + code + '</xmp>'
                } else
                {
                    return '<div style="word-break:break-all;white-space: normal;">'+code.replace(/\n/g,'<br/>').replace(/\tat/g,'')+"</div>";
                }
            }
        }
    };
</script>