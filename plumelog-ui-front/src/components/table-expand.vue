<style lang="less" scoped>

  .detail_table {
    td {
      background: none;
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
              <td class="key">服务器时间</td>
              <td class="value">{{ row.dateTime }}</td>
            </tr>
            <tr>
              <td class="key">类名</td>
              <td class="value">{{ row.className }}</td>
            </tr>
            <tr>
              <td class="key">方法名</td>
              <td class="value">{{ row.method }}</td>
            </tr>
            <tr v-if="row.threadName">
              <td class="key">线程名</td>
              <td class="value">{{ row.threadName }}</td>
            </tr>
            <tr>
              <td class="key">内容</td>
              <td class="value">
                <div style="white-space: pre-wrap;">
                <span v-for="item in highLightCode(row.highlightCnt || row.content, !!row.highlightCnt)">
                    <span v-if="item.isH" v-html="item.content"></span>
                    <span v-else>{{ item.content }}</span>
                </span>
                </div>
              </td>
            </tr>
          </table>
        </span>
      </Col>
    </Row>
  </div>
</template>
<script>
    let isHtml = /<\/?[a-z][\s\S]*>/i
    export default {
        props: {
            searchKey: String,
            row: Object,
        },
        methods: {
            highLightCode(code, isHighlight) {
                code = code.replace(/\\n/g, '\n').replace(/\\t/g, '\t');
                let rows = [];
                if (code.indexOf('java.') > -1) {
                    let content = '<pre style="word-break:break-all;white-space: normal;">' + Prism.highlight(code.replace(/\n/g, '<br/>').replace(/\t/g, '　　'), Prism.languages.stackjava, 'stackjava').replace(/&lt;/g, '<').replace(/&gt;/g, '>') + "</pre>"
                    rows.push({isH: true, content})
                    return rows;
                } else if (isHtml.test(code)) {
                    let content = code;
                    if (isHighlight) {
                        let h = [];
                        let hContent = code.match(/<em>([\s\S]*?)<\/em>/g);
                        code = code.replace(/<em>([\s\S]*?)<\/em>/g, "@Highlight@")

                        var strings = code.split('@');
                        strings = strings.filter(x => !!x)
                        // console.log(code)
                        // console.log(hContent)
                        // console.log(strings)
                        let hi = 0
                        for (let i = 0; i < strings.length; i++) {
                            let isH = strings[i] === 'Highlight';
                            let content = isH ? hContent[hi] : strings[i];
                            h.push({isH, content})
                            isH && hi++
                        }
                        rows.push(...h)
                        // console.log(rows)
                        return rows;
                    }
                    rows.push({isH: false, content})
                    return rows;
                } else {
                    let content = '<div style="word-break:break-all;white-space: normal;">' + code.replace(/\n/g, '<br/>').replace(/\t/g, '　　') + "</div>";
                    rows.push({isH: true, content})
                    return rows;
                }
            }
        }
    };
</script>
