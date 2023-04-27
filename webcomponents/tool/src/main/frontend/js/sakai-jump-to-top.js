import { loadProperties } from './sakai-i18n.js';

const SCROLL_THRESHOLD = 500; // px

// DOM Elements
const portalMainContainer = document.querySelector('.portal-main-container');
const jumpToTopButton = document.createElement('button');
jumpToTopButton.classList.add('jump-to-top');
jumpToTopButton.insertAdjacentHTML('beforeend', '<i class="si si-arrow-up-circle-fill"></i>');
portalMainContainer.appendChild(jumpToTopButton);

// i18n variable
let jumptotopLabel;

// load i18n properties and initialize the component when ready
async function init() {
  const i18n = await loadProperties('jumptotop');
  jumptotopLabel = i18n['jumptotop_title'];
  jumpToTopButton.title = jumptotopLabel;
  jumpToTopButton.ariaLabel = jumptotopLabel;
}

// show/hide jump to top button based on scroll position
function handleScroll() {
  if (portalMainContainer.scrollTop > SCROLL_THRESHOLD) {
    jumpToTopButton.classList.add('show');
  } else {
    jumpToTopButton.classList.remove('show');
  }
}

// scroll to top of the page when jump to top button is clicked
function handleJumpToTopClick() {
  const scrollOptions = {
    top: 0,
    behavior: 'smooth'
  };
  portalMainContainer.scrollTo(scrollOptions);
}

// initialize component
init();

portalMainContainer.addEventListener('scroll', handleScroll);
jumpToTopButton.addEventListener('click', handleJumpToTopClick);
