({
	init: function() {
		this.$index = $('.index-container');
		this.$itemList = $('.item-list');
		this.initFunction();
		var param = this.getParams();
		this.loadPage(param.type);
	},
	initFunction: function() {
		var self = this;
		$('.js-pageSwitch a').click(function() {
			var type = $(this).data('type');
			location.hash = 'type=' + type;
			self.loadPage(type);
		});
	},
	loadPage: function(type) {
		if (!type) {
			this.$index.show();
			return;
		}
		
		this.$index.hide();
		this.$itemList.show();
		this.$itemList.text(type);
		$('.js-pageSwitch a').removeClass('active');
		$('.js-pageSwitch a[data-type=' + type + ']').addClass('active');
	},
	loadItemList: function() {
		var title = '万代模型 SDCS 高达 巴巴托斯 天狼帝王型';
		var img = 'https://img.alicdn.com/imgextra/i3/833261111/O1CN017SxBhi1K4scwDMp3d_!!833261111-2-lubanu-s.png_430x430q90.jpg';
	},
	getParams: function() {
		var search = location.hash;
		if (search.startsWith('#')) {
			search = search.substring(1);
		} 
		var paramArr = search.split('&');
		var params = {};
		paramArr.forEach((a) => {
			var arr = a.split('=');
			if (arr.length > 1) {
				params[arr[0]] = arr[1];
			}
		});
		
		console.log(params);
		return params;
	}
}).init();