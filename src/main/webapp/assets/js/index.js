({
	init: function() {
		this.$index = $('.index-container');
		this.$itemListContainer = $('.item-list-container');
		this.$pageSwitch = $('.js-pageSwitch');
		this.itemListTpl = $('#itemListTpl').html();
		this.$moreBtn = $('.js-moreBtn');
		this.$modal = $('.detail-img-modal');
		this.items = [];
		this.size = 20;
		this.initFunction();
		var param = this.getParams();
		this.loadPage(param.type);
	},
	initFunction: function() {
		var self = this;
		self.$pageSwitch.on('click', 'a:not(.active)', function() {
			var type = $(this).data('type');
			location.hash = 'type=' + type;
			self.loadPage(type);
		});
		$(document).scroll(function(e) {
			var scrollTop = document.documentElement.scrollTop || document.body.scrollTop;
			var clientHeight = document.documentElement.clientHeight || document.body.clientHeight;
			var scrollHeight = document.documentElement.scrollHeight || document.body.scrollHeight;
			if (scrollHeight > clientHeight && scrollTop + clientHeight === scrollHeight) {
				self.loadItemList();
			}
		});
		$(document).on('click', '.js-cover-img', function() {
			var itemId = $(this).data('itemid');
			$.get('/gundam/getItemImgs?itemId=' + itemId, function(resp) {
				if (resp.code == 0) {
					console.log(resp.data);
					var itemImgs = resp.data;
					if (itemImgs.length == 0) {
						return;
					}
					var html = '';
					for (itemImg of itemImgs) {
						var img = '<img src="' + itemImg.localDetailImg + '">';
						html += img;
					}
					
					self.$modal.find('.modal-body p').html(html);
					self.$modal.modal('show');
				}
			});
		});
		self.$moreBtn.click(function() {
			self.loadItemList();
		});
	},
	loadPage: function(type) {
		var self = this;
		if (!type) {
			self.$index.show();
			return;
		}
		
		self.$pageSwitch.find('a').removeClass('active');
		self.$pageSwitch.find('a[data-type=' + type + ']').addClass('active');
		self.$itemListContainer.find('.breadcrumb .active').text(type);
		self.$index.hide();
		self.$itemListContainer.show();
		self.$itemListContainer.find('.item-list').html('');
		self.getAndloadItemList(type);
	},
	getAndloadItemList: function(type) {
		var self = this;
		self.items = [];
		console.log('type: ', type);
		$.get('/gundam/getModelItems?type=' + type, function(resp) {
			if (resp.code != 0) {
				return;
			}
			self.items = resp.data;
			self.loadItemList();
		});
	},
	loadItemList: function() {
		var self = this;
		console.log(self.items);
		if (self.items.length == 0){
			return;
		}
		var items = self.items.splice(0, self.size);
		var html = template.compile(self.itemListTpl)({items: items});
		self.$itemListContainer.find('.item-list').append(html);
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