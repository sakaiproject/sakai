import { defineStore } from 'pinia'

export const useDataStore = defineStore('data', {
  state: () => {
    return { 
      storedData: {}
    }
  },
  actions: {
    clearStoredData() {
        this.storedData = {};
    },
  },
})