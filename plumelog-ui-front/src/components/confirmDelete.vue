<template>
    <div>
        <div class="modal fade show model_pwd" style="display:block" v-if="show" tabindex="-1" role="dialog">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">管理密码</h5>
                    <button type="button" class="close" @click="closeModal" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                    </button>
                </div>
                <div class="modal-body">
                    <p><input type="password" autofocus v-model="password" placeholder="输入管理员密码" class="form-control"  /></p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-dismiss="modal" @click="closeModal">关闭</button>
                    <button type="button" class="btn btn-primary" @click="confirmModal">确认</button>
                </div>
                </div>
            </div>
        </div>
        <div class="modal-backdrop fade show" v-if="show"></div>
    </div>
    
</template>
<script>
export default {
    name: "confirmDelete",
    data(){
        return {
            show: this.value,
            password: '',
        }
    },
    props:{
        value: {
            type: Boolean,
            default: false
        },
    },
    watch: {
        value (val) {
            this.show = val
        },
        show (val) {
            this.$emit('input', val)
        }
    },
    computed:{
       
    },
    mounted(){
        
    },
    methods:{
        closeModal(){
            this.show = false;
            this.password = '';
        },
        confirmModal(){
            this.show = false;
            this.$emit('on-confirm', this.password);
            this.password = '';
        }
    }
}
</script>
<style lang="less">

</style>