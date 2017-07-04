function CommonsPermissions(data) {

	if (!data) return;

	this.postReadAny = false;
	this.postCreate = false;
	this.postDeleteAny = false;
	this.postDeleteOwn = false;
	this.postUpdateAny = false;
	this.postUpdateOwn = false;
	this.commentCreate = false;
	this.commentDeleteAny = false;
	this.commentDeleteOwn = false;
	this.commentUpdateAny = false;
	this.commentUpdateOwn = false;
	this.updateSite = false;

	for(var i=0,j=data.length;i<j;i++) {
		if('commons.post.read.any' === data[i])
			this.postReadAny = true;
		else if('commons.post.create' === data[i])
			this.postCreate = true;
		else if('commons.post.delete.any' === data[i])
			this.postDeleteAny = true;
		else if('commons.post.delete.own' === data[i])
			this.postDeleteOwn = true;
		else if('commons.post.update.any' === data[i])
			this.postUpdateAny = true;
		else if('commons.post.update.own' === data[i])
			this.postUpdateOwn = true;
		else if('commons.comment.create' === data[i])
			this.commentCreate = true;
		else if('commons.comment.delete.any' === data[i])
			this.commentDeleteAny = true;
		else if('commons.comment.delete.own' === data[i])
			this.commentDeleteOwn = true;
		else if('commons.comment.update.any' === data[i])
			this.commentUpdateAny = true;
		else if('commons.comment.update.own' === data[i])
			this.commentUpdateOwn = true;
		else if('site.upd' === data[i])
			this.updateSite = true;
	}
}
