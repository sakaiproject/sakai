<template>
	<div class='timerBlock' aria-hidden='true'>
		<b-collapse v-model="showProgress" visible id="wrapper">
			<div v-if="text" class="timer-title">{{ text }}</div>
			<div class="progress-wrapper">
				<div class="progress-label-wrapper">
					{{ timeValue }}
				</div>
				<b-progress :value="value" :max="timeLimit" :variant="computedColor" />
			</div>
		</b-collapse>

		<div class="warn-banner" v-if="showWarning">
			<b-alert show variant="warning" class="mb-2">
				{{ i18n["msg.time_over_soon"] }}
				<a @click="closeWarning">{{ i18n["btn.close"] }}</a>
			</b-alert>
		</div>

		<b-button v-b-toggle:wrapper class="showHide">
			<span class="when-open">
				<span class="showHideSym">▲</span>
				<span class="showHideText">{{ i18n["msg.hide"] }}</span>
				<span class="showHideSym">▲</span>
			</span>
			<span class="when-close">
				<span class="showHideSym">▼</span>
				<span class="showHideText">{{ i18n["msg.show"] }}</span>
				<span class="showHideSym">▼</span>
			</span>
		</b-button>
	</div>
</template>

<style lang="scss">
	@import 'bootstrap/dist/css/bootstrap.css';
	@import "../bootstrap-styles/progress.scss";
	@import "../bootstrap-styles/alert.scss";

	.timerBlock {
		background-color: var(--sakai-primary-color-1);
		border-radius: 10px;

		.timer-title {
			padding: 12px 0 0;
			color: var(--sakai-text-color-inverted);
			font-weight: bold;
			text-align: center;
		}

		.progress-wrapper {
			display: flex;
			
			.progress-label-wrapper {
				display: flex;
				justify-content: center;
				align-items: center;
				width: 100px;
				margin: 12px 0px 0px 12px;
				overflow: hidden;
				background-color: var(--sakai-background-color-4);
				border-radius: 4px;
				box-shadow: 0 2px 2px rgba(0,0,0,0.1) inset;
				font-weight: bold;
				color: var(--sakai-text-color-1);
				font-size: 16px;
			}
			.progress {
				margin: 12px 12px 0;
				height: 3rem;
				width: 100%;
		
				.progress-bar {
					transition: all 0.6s ease;
				}
			}
		}

		.warn-banner {
			padding-top: 12px;

			& > .alert {
				margin-top: 0;
				margin-left: 12px;
				margin-right: 12px;
		
				a {
					color: var(--warnBanner-color);
					text-decoration: underline;
					float: right;
		
					&:hover {
						cursor: pointer;
						color: var(--link-hover-color);
						text-decoration: underline;
					}
				}
			}
		}

		.showHide {
			background-color: transparent;
			border:none;
			display: flex;
			margin: 5px auto ;

			&:active {
				background-color: transparent;
			}
			
			&.collapsed > .when-open,
			&.not-collapsed > .when-close {
				display: none;
			}

			.showHideText {
				padding: 0 3px;
				color: var(--sakai-text-color-inverted);
				font-weight: bold;
			}
			
			.showHideSym {
				color: var(--sakai-text-color-inverted);
			}
		}
	}
</style>

<script>
import {
	BProgress, BButton, BAlert, BCollapse, VBToggle
} from 'bootstrap-vue';
import {  fetchJson } from "../utils/core-utils.js";
import i18nMixin from "../mixins/i18n-mixin.js";

export default {
	name: "timer-bar",
	components: {
		BProgress, BButton, BAlert, BCollapse
	},
	directives: {
		'b-toggle': VBToggle
	},
	mixins: [i18nMixin],
	created () {
		this.origin = window.location.origin;
		this.startTimer();
	},
	data() {
		return {
			origin: "",
			value: Math.max(this.timeLimit - this.timeElapsed, 0),
			intervalId1: null,
			intervalId2: null,
			saveSended: false,
			showProgress: true,
			closedWarning: false
		}
	},
	props: {
		id: { type: String },
		text: { type: String },
		timeLimit: { type: Number },
		timeElapsed: { type: Number },
		syncCall: { type: String }
	},
	watch: {
		value(newV, oldV) {
			//save values 5 secs before end
			if(this.value <= 5 && !this.saveSended){
				this.sendSaveMessage();
				this.saveSended = true;
			}

			if(this.value <= 0){
				clearInterval(this.intervalId1);
				clearInterval(this.intervalId2);
				this.sendEndMessage();
			}
		}
	},
	computed: {
		progressValue() {
			return 100 - Math.floor(((this.timeLimit - this.value) / this.timeLimit) * 100);
		},
		timeValue() {
			const hours = String(Math.floor(this.value / 3600)).padStart(2, '0');
			const minutes = String(Math.floor((this.value % 3600) / 60)).padStart(2, '0');
			const seconds = String(this.value % 60).padStart(2, '0');
			return `${hours}:${minutes}:${seconds}`;
		},
		computedColor() {
			if (this.progressValue >= 50) {
				return "success";
			} else if (this.progressValue <= 25) {
				return "danger";
			} else {
				return "warning";
			}
		},
		showWarning() {
			return !this.closedWarning && !this.showProgress && this.progressValue < 10;
		}
	},
	methods: {
		startTimer() {
			//every second, update the timer
			this.intervalId1 = setInterval(() => {
				this.value -= 1;
			}, 1000);

			if(this.syncCall){
				//every minute, resync timer with backend
				this.intervalId2 = setInterval(this.synchronizeTimer, 60000);
			}
		},
		closeWarning() {
			this.closedWarning = true;
		},
		sendSaveMessage() {
			let data = {
				id: this.id,
				msg: "SAVE"
			}
			window.parent.postMessage(data, this.origin);
		},
		sendEndMessage() {
			let data = {
				id: this.id,
				msg: "END"
			}
			window.parent.postMessage(data, this.origin);
		},
		//get time elapsed from server and update local value
		async synchronizeTimer() {
			let data = await fetchJson(this.syncCall);
			if(data && data.id == this.id){
				if (data.timeElapsed && data.timeElapsed > 0){
					this.value = Math.max(this.timeLimit - data.timeElapsed, 0);
				}
			}
		}
	}
};
</script>
