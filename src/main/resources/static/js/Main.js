/**
 * Textro PdfCraft - Main JavaScript File
 * Handles file upload, drag & drop, theme switching, and core UI interactions
 */

class PdfCraftApp {
    constructor() {
        this.uploadedFiles = new Map();
        this.currentTheme = localStorage.getItem('theme') || 'light';
        this.maxFileSize = 50 * 1024 * 1024; // 50MB
        this.supportedFormats = {
            'application/pdf': 'pdf',
            'application/vnd.openxmlformats-officedocument.wordprocessingml.document': 'docx',
            'application/vnd.openxmlformats-officedocument.presentationml.presentation': 'pptx',
            'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': 'xlsx',
            'image/png': 'png',
            'image/jpeg': 'jpg',
            'image/jpg': 'jpg'
        };

        this.init();
    }

    init() {
        this.setupEventListeners();
        this.initTheme();
        this.loadRecentFiles();
        this.setupDragAndDrop();
        this.setupFileUpload();
    }

    /**
     * Setup all event listeners
     */
    setupEventListeners() {
        // Theme toggle
        const themeToggle = document.getElementById('themeToggle');
        if (themeToggle) {
            themeToggle.addEventListener('click', () => this.toggleTheme());
        }

        // File input change
        const fileInput = document.getElementById('fileInput');
        if (fileInput) {
            fileInput.addEventListener('change', (e) => this.handleFileSelect(e));
        }

        // Quick action buttons
        this.setupQuickActions();

        // Window resize handler
        window.addEventListener('resize', () => this.handleResize());

        // Keyboard shortcuts
        document.addEventListener('keydown', (e) => this.handleKeyboardShortcuts(e));
    }

    /**
     * Initialize theme
     */
    initTheme() {
        const html = document.documentElement;
        const themeIcon = document.getElementById('themeIcon');

        html.setAttribute('data-bs-theme', this.currentTheme);

        if (themeIcon) {
            themeIcon.className = this.currentTheme === 'dark'
                ? 'bi bi-sun-fill'
                : 'bi bi-moon-fill';
        }
    }

    /**
     * Toggle between light and dark themes
     */
    toggleTheme() {
        const html = document.documentElement;
        const themeIcon = document.getElementById('themeIcon');

        // Add transition class
        html.classList.add('theme-transition');

        this.currentTheme = this.currentTheme === 'light' ? 'dark' : 'light';
        html.setAttribute('data-bs-theme', this.currentTheme);

        if (themeIcon) {
            themeIcon.className = this.currentTheme === 'dark'
                ? 'bi bi-sun-fill'
                : 'bi bi-moon-fill';
        }

        localStorage.setItem('theme', this.currentTheme);

        // Remove transition class after animation
        setTimeout(() => {
            html.classList.remove('theme-transition');
        }, 300);
    }

    /**
     * Setup drag and drop functionality
     */
    setupDragAndDrop() {
        const dropZone = document.getElementById('dropZone');
        if (!dropZone) return;

        ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
            dropZone.addEventListener(eventName, this.preventDefaults, false);
        });

        ['dragenter', 'dragover'].forEach(eventName => {
            dropZone.addEventListener(eventName, () => this.handleDragEnter(dropZone), false);
        });

        ['dragleave', 'drop'].forEach(eventName => {
            dropZone.addEventListener(eventName, () => this.handleDragLeave(dropZone), false);
        });

        dropZone.addEventListener('drop', (e) => this.handleDrop(e), false);
        dropZone.addEventListener('click', () => this.triggerFileInput());
    }

    /**
     * Prevent default drag behaviors
     */
    preventDefaults(e) {
        e.preventDefault();
        e.stopPropagation();
    }

    /**
     * Handle drag enter
     */
    handleDragEnter(dropZone) {
        dropZone.classList.add('drag-over');
    }

    /**
     * Handle drag leave
     */
    handleDragLeave(dropZone) {
        dropZone.classList.remove('drag-over');
    }

    /**
     * Handle file drop