<template>
	<div v-if="!isEmpty">
		<multiselect
			:value="value"
			:options="options"
			:multiple="true"
			group-values="items"
			group-label="name"
			:selected-label="i18n.selected"
			:select-label="i18n.select"
			:deselect-label="i18n.deselect"
			:placeholder="i18n.search_filter"
			track-by="name"
			label="name"
			@input="selectUnique"
		>
			<span slot="noOptions">{{ i18n.no_options }}</span>
			<span slot="noResult">{{ i18n.no_results }}</span>
		</multiselect>
		<input type="hidden" name="groups[]" v-for="v in value" :value="v.name" />
	</div>
</template>
<style src="vue-multiselect/dist/vue-multiselect.min.css"></style>
<style type="scss">

.multiselect__placeholder {
	display: inline-block !important;
	margin-bottom: 0px !important;
	padding-top: 0px !important;
	color: var(--sakai-text-color-dimmed) !important;
}
.multiselect__option {
	/* Needs to apply to ...option and ...option::after */
	&,
	&::after {
		color: var(--sakai-text-color-1) !important;
		background: var(--sakai-background-color-1) !important;
	}
	&.multiselect__option--selected {
		&,
		&::after {
			color: var(--sakai-text-color-2) !important;
			background: var(--sakai-background-color-2) !important;
		}
	}
	&.multiselect__option--highlight {
		&,
		&::after {
			color: var(--infoBanner-color) !important;
			background: var(--infoBanner-bgcolor) !important;
		}
		&.multiselect__option--selected {
			&,
			&::after {
				color: var(--errorBanner-color) !important;
				background: var(--errorBanner-bgcolor) !important;
			}
		}
	}
}

.multiselect__input {
	background: var(--sakai-background-color-1) !important;
	color: var(--sakai-text-color-1) !important;
	&::placeholder {
		color: var(--sakai-text-color-dimmed) !important;
	}
}

.multiselect__input,
.multiselect__single {
	margin-top: 10px;
}
.multiselect__content-wrapper {
	border-color: var(--sakai-border-color);
}
</style>

<script>

import Multiselect from "vue-multiselect";
import i18nMixin from "../mixins/i18n-mixin.js";
// Enable component api
import "../components-api/gb-selector-input-sync.js";
export default {
	name: "gb-selector",
	components: {
		Multiselect,
	},
	mixins: [i18nMixin],
	props: {
		collectionId: { type: String },
		itemId: { type: String },
		siteId: { type: String },
		tool: { type: String },
		selectedTemp: { type: String },
		isCategory: { type: Boolean },
		userId: { type: String },
		appName: { type: String },
	},
	data(props) {
		return {
			value: [],
			options: [],
			isEmpty: false
		};
	},
	computed: {},
	watch: {
		value(value) {
			this.$emit("change", JSON.parse(JSON.stringify(value)));
		},
	},
	methods: {
		selectUnique(ev) {
			if (!ev || ev.length < this.value.length) {
				this.value = ev;
				return;
			}
			let newValue = ev.filter((x) => this.value.indexOf(x) === -1)[0];
			let group = this.getGradebookByItem(newValue);
			if (this.value.some((x) => this.getGradebookByItem(x) === group)) {
				this.value = this.value.filter((x) => this.getGradebookByItem(x) !== group);
				this.value.push(newValue);
			} else this.value = ev;
		},

		getGradebookByItem(givenItem){
			return this.options.filter((gradebook) =>
				gradebook.items.some((item) => item.name === givenItem.name)
			)[0].name;
		}
	},
	async mounted() {
		var endpoint = this.isCategory ? "/categories" : "/items/" + this.appName;
		console.debug("ENDPOINT: " + endpoint);

		if (this.userId) {
			endpoint += "/" + this.userId;
		}

		await fetch('/api/sites/' + this.siteId + endpoint)
		.then((r) => {
			if (r.ok) {
				return r.json();
			}
			throw new Error(`Failed to get items for site ` + this.siteId);
		})
		.then((data) => {
			console.debug(data);
			if (data != null && data.length > 0) {
				this.options = data.map(parent => {
					parent.items = parent.items.map(item => {
						// we add the group name to differentiate the items plus to avoid clashes in the multiselect
						item.name = `${parent.name} - ${item.name}`;
						return item;
					});
					return parent;
				});
				const allItemsEmpty = this.options.every(group => group.items.length === 0);
				if (allItemsEmpty) {
					this.isEmpty = true;
				} else {
					this.isEmpty = false;
				}
			} else {
				this.options = [{ name: 'No options found', items: [] }];
			}
		})
		.catch ((error) => console.error(error));

		this.selectedTemp.split(',').forEach((element) => {
			for (const option of this.options) {
				const foundItem = option.items.find((item) => item.id === element);
				if (foundItem) {
					this.value.push(foundItem);
					break;
				}
			}
		});
	},
};
</script>
