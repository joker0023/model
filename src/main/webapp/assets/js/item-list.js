({
	init: function() {
		this.type = '';
		this.page = 1;
		this.size = 20;
		this.items = {};
		this.$itemList = $('.js-itemList');
		this.$pager = $('.pager');
		this.tplSource = $('#listTemplate').html();
		this.$tabChange = $('.js-tabChange');
		this.$operationContainer = $('.js-operationContainer');
		this.$modal = $('.detail-img-modal');
		
		this.initFunction();
		
	},
	initFunction: function() {
		var self = this;
		
		self.$tabChange.on('click', '.btn', function() {
			var type = $(this).data('type');
			if (self.type == type) {
				return;
			}
			
			$('.breadcrumb .active').text($(this).text());
			self.type = type;
			self.page = 1;
			self.listItems(self.type, self.page);
		});
		
		self.$pager.on('click', '.previous:not(.disabled) a', function() {
			self.page -= 1;
			if (self.page < 1) {
				self.page = 1;
				return;
			}
			self.listItems(self.type, self.page);
			$('html,body').animate({scrollTop:0}, 100);
		}).on('click', '.next:not(.disabled) a', function() {
			self.page += 1;
			self.listItems(self.type, self.page);
//			document.documentElement.scrollTop = 0;
			$('html,body').animate({scrollTop:0}, 100);
		});
		
		self.$itemList.on('click', '.js-toggle:not(.disabled)', function() {
			$(this).addClass('disabled');
			
			var id = $(this).parent().data('itemid');
			var item = self.items[id];
			$.post('/console/spider/toggleOpen', {id: item.id}, function(resp) {
				if (resp.code == 0) {
					self.listItems(self.type, self.page);
				} else {
					alert('error: ' + resp.errorMsg);
				}
			});
		}).on('click', '.js-spider:not(.disabled)', function() {
			var $btn = $(this);
			$btn.addClass('disabled');
			
			var id = $(this).parent().data('itemid');
			var item = self.items[id];
			$.post('/console/spider/spiderItem', {id: item.id}, function(resp) {
				if (resp.code == 0) {
					self.listItems(self.type, self.page);
				} else {
					alert('error: ' + resp.errorMsg);
					$btn.removeClass('disabled');
				}
			});
		}).on('click', '.js-title', function() {
			var itemId = $(this).data('itemid');
			$.get('/console/spider/getItemImgs?itemId=' + itemId, function(resp) {
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
		
		self.$operationContainer.on('click', '.js-spiderPageListBtn:not(.disabled)', function() {
			if (!self.type) {
				return;
			}
			var $btn = $(this);
			$btn.addClass('disabled');
			$.post('/console/spider/spiderList', {type: self.type}, function(resp) {
				if (resp.code == 0) {
					self.listItems(self.type, self.page);
				} else {
					alert('error: ' + resp.errorMsg);
					$btn.removeClass('disabled');
				}
			});
		}).on('click', '.js-spiderPageItemsBtn:not(.disabled)', function() {
			var ids = '';
			for (var id in self.items) {
				ids += ',' + id;
			}
			if (!self.type || !ids) {
				return;
			}
			ids = ids.substring(1);
			var $btn = $(this);
			$btn.addClass('disabled');
			$.post('/console/spider/spiderItems', {ids: ids}, function(resp) {
				if (resp.code == 0) {
					$btn.removeClass('disabled');
				}
			});
		});
	},
	listItems: function(type, page) {
		var self = this;
		var url = '/console/spider/getItems?type={type}&page={page}&size={size}';
		url = url.replace('{type}', type).replace('{page}', page).replace('{size}', self.size);
		$.get(url, function (resp) {
			console.log(resp);
			var pageInfo = resp.data;
			if (pageInfo.hasPreviousPage) {
				self.$pager.find('.previous').removeClass('disabled');
			} else {
				self.$pager.find('.previous').addClass('disabled');
			}
			if (pageInfo.hasNextPage) {
				self.$pager.find('.next').removeClass('disabled');
			} else {
				self.$pager.find('.next').addClass('disabled');
			}
			self.$pager.find('.active a').text(pageInfo.pageNum + '/' + pageInfo.pages + '_(' + pageInfo.total + ')');
			
			self.items = {};
			var items = pageInfo.list;
			for (var item of items) {
				self.items[item.id] = item;
			}
			var html = template.compile(self.tplSource)({items:items});
			self.$itemList.html(html);
		});
	}
	// end
}).init();