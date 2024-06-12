<template>
	<div>
<!-- TODO revisar labels segun items o categorias -->
		<span>{{ i18n.groups }}</span>
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
			<!--<span slot="noResult">Oops! No elements found. Consider changing the search query.</span>-->
			<span slot="noOptions">{{ i18n.no_options }}</span>
			<span slot="noResult">{{ i18n.no_results }}</span>
		</multiselect>
		<pre class="language-json"><code>{{ value  }}</code></pre>
		<input type="hidden" name="groups[]" v-for="v in value" :value="v.name" />
	</div>
</template>
<style src="vue-multiselect/dist/vue-multiselect.min.css"></style>
<style type="scss">
/*TODO S2U-26 MOVE TO A COMMON PLACE WITH THE TAG SCSS?? */
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
.multiselect__tags {
	padding: 5px !important;
	min-height: 34px;
	background: var(--sakai-background-color-1) !important;
	border-color: var(--sakai-border-color);
}
.multiselect__input {
	background: var(--sakai-background-color-1) !important;
	color: var(--sakai-text-color-1) !important;
	&::placeholder {
		color: var(--sakai-text-color-dimmed) !important;
	}
}
.multiselect__tag {
	display: inline-block;
	min-width: 10px;
	font-size: 12px;
	font-weight: bold;
	line-height: 1;
	vertical-align: middle;
	white-space: nowrap;
	text-align: center;
	border-radius: 10px;
	margin-bottom: 0px !important;
	margin-right: 5px !important;
	color: var(--infoBanner-color) !important;
	background: var(--infoBanner-bgcolor) !important;
}
.multiselect__tag-icon {
	border-radius: 0px;
	&::after {
		color: var(--infoBanner-color) !important;
	}
	&:hover,
	&:focus {
		background: color-mix(
			in hsl,
			var(--infoBanner-bgcolor) 80%,
			var(--sakai-text-color-1)
		) !important;
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
		gradebookList: String,
		collectionId: { type: String },
		itemId: { type: String },
		siteId: { type: String },
		tool: { type: String },
		selectedTemp: { type: String },
		extraOptions: { type: String },
		isCategory: { type: Boolean },
	},
	data(props) {
		console.log("PROPS: ", props.gradebookList);
		    return {
				value: [],
				options: [],
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
    	await fetch('/api/sites/' + this.siteId + '/items')
      	.then((r) => {
			if (r.ok) {
				return r.json();
			}
        	throw new Error(`Failed to get items for site ` + this.siteId);
      	})
      	.then((data) => {
        	console.log("DATA", data);
			this.options = data;
      	})
      	.catch ((error) => console.error(error));
		console.log("TEST 2");
	},
};
</script>
