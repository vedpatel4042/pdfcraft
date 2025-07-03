// // Enhanced PDF Editor JavaScript with full backend integration
// class PdfEditor {
//     constructor(sessionId = null, pageNumber = 1) {
//         this.sessionId = sessionId;
//         this.currentPage = pageNumber;
//         this.zoomLevel = 1.0;
//         this.isEditing = false;
//         this.selectedTextId = null;
//         this.pdfDoc = null;
//         this.pageCount = 1;
        
//         if (sessionId) {
//             this.init();
//         }
//     }

//     init() {
//         this.bindEvents();
//         this.loadDocument();
//     }

//     bindEvents() {
//         // Bind UI events from editor.html
//         document.getElementById('prevPageBtn')?.addEventListener('click', () => this.previousPage());
//         document.getElementById('nextPageBtn')?.addEventListener('click', () => this.nextPage());
//         document.getElementById('zoomInBtn')?.addEventListener('click', () => this.zoomIn());
//         document.getElementById('zoomOutBtn')?.addEventListener('click', () => this.zoomOut());
//         document.getElementById('saveDocBtn')?.addEventListener('click', () => this.saveDocument());
//         document.getElementById('downloadBtn')?.addEventListener('click', () => this.downloadPdf());
//         document.getElementById('addPageBtn')?.addEventListener('click', () => this.addPage());
//         document.getElementById('deletePageBtn')?.addEventListener('click', () => this.deletePage());
//         document.getElementById('rotateLeftBtn')?.addEventListener('click', () => this.rotatePage(-90));
//         document.getElementById('rotateRightBtn')?.addEventListener('click', () => this.rotatePage(90));
//         document.getElementById('mergeBtn')?.addEventListener('click', () => this.showMergeModal());
//         document.getElementById('splitBtn')?.addEventListener('click', () => this.showSplitModal());
//         document.getElementById('compressBtn')?.addEventListener('click', () => this.compressPdf());
        
//         // Text editing events
//         document.getElementById('pdfPage')?.addEventListener('click', (e) => this.handlePdfClick(e));
//         document.getElementById('pdfPage')?.addEventListener('contextmenu', (e) => {
//             e.preventDefault();
//             this.handleRightClick(e);
//         });
        
//         // Modal events
//         document.getElementById('saveEditBtn')?.addEventListener('click', () => this.saveEdit());
//         document.getElementById('cancelEditBtn')?.addEventListener('click', () => this.cancelEdit());
//         document.getElementById('deleteTextBtn')?.addEventListener('click', () => this.deleteText());
        
//         // Merge/Split events
//         document.getElementById('mergeFileInput')?.addEventListener('change', (e) => this.handleMergeFile(e));
//         document.getElementById('executeSplitBtn')?.addEventListener('click', () => this.splitPdf());
//     }

//     // =====================
//     // PDF Document Operations
//     // =====================

//     // Add to your PdfEditor class
// async loadDocument() {
//     try {
//         if (!this.sessionId) {
//             throw new Error('No session ID available');
//         }

//         // Load PDF document info
//         const info = await this.getPdfInfo();
//         this.pageCount = info.pageCount;
//         this.updatePageDisplay();
        
//         // Load the first page
//         await this.loadPage();
        
//         // Hide placeholder and show PDF
//         document.getElementById('pdfPlaceholder').style.display = 'none';
//         document.getElementById('pdfCanvas').style.display = 'block';
//     } catch (error) {
//         console.error('Error loading document:', error);
//         this.showNotification('Error loading PDF document', 'error');
//     }
// }

// async loadPage() {
//     try {
//         // Try to render with PDF.js first
//         const pdfUrl = `/api/editor/session/${this.sessionId}/pdf`;
//         console.log("Loading PDF from", pdfUrl);
//         this.pdfDoc = await pdfjsLib.getDocument(pdfUrl).promise;
        
//         const page = await this.pdfDoc.getPage(this.currentPage);
//         const canvas = document.getElementById('pdfCanvas');
//         const context = canvas.getContext('2d');
//         const viewport = page.getViewport({ scale: this.zoomLevel });
        
//         canvas.height = viewport.height;
//         canvas.width = viewport.width;
        
//         await page.render({
//             canvasContext: context,
//             viewport: viewport
//         }).promise;
        
//         this.updatePageDisplay();
//     } catch (pdfJsError) {
//         console.log('PDF.js rendering failed, falling back to image render');
//         // Fallback to image rendering if PDF.js fails
//         const response = await fetch(`/api/pdf/page/${this.sessionId}/${this.currentPage}?dpi=150`);
//         if (!response.ok) throw new Error(`Failed to load page: ${response.statusText}`);

//         const blob = await response.blob();
//         const imageUrl = URL.createObjectURL(blob);
//         const pdfImage = document.getElementById('pdfImage');
        
//         pdfImage.src = imageUrl;
//         pdfImage.style.display = 'block';
//         pdfImage.style.transform = `scale(${this.zoomLevel})`;
//     }
// }
//     // async loadDocument() {
//     //     try {
//     //         if (!this.sessionId) {
//     //             throw new Error('No session ID available');
//     //         }

//     //         // Load PDF document info
//     //         const info = await this.getPdfInfo();
//     //         this.pageCount = info.pageCount;
//     //         this.updatePageDisplay();
            
//     //         // Load the first page
//     //         await this.loadPage();
//     //     } catch (error) {
//     //         console.error('Error loading document:', error);
//     //         this.showNotification('Error loading PDF document', 'error');
//     //     }
//     // }

//     // async loadPage() {
//     //     try {
//     //         const response = await fetch(`/api/pdf/page/${this.sessionId}/${this.currentPage}?dpi=150`);
//     //         if (!response.ok) {
//     //             throw new Error(`Failed to load page: ${response.statusText}`);
//     //         }

//     //         const blob = await response.blob();
//     //         const imageUrl = URL.createObjectURL(blob);
//     //         const pdfPage = document.getElementById('pdfPage');
//     //         if (pdfPage) {
//     //             pdfPage.src = imageUrl;
//     //             pdfPage.style.transform = `scale(${this.zoomLevel})`;
//     //         }
//     //     } catch (error) {
//     //         console.error('Error loading page:', error);
//     //         this.showNotification('Error loading PDF page', 'error');
//     //     }
//     // }

//     async getPdfInfo() {
//         try {
//             const response = await fetch(`/api/pdf/info/${this.sessionId}`);
//             if (!response.ok) {
//                 throw new Error(`Failed to get PDF info: ${response.statusText}`);
//             }
//             return await response.json();
//         } catch (error) {
//             console.error('Error getting PDF info:', error);
//             throw error;
//         }
//     }

//     // =====================
//     // Page Navigation & Zoom
//     // =====================

//     async previousPage() {
//         if (this.currentPage > 1) {
//             this.currentPage--;
//             await this.loadPage();
//             this.updatePageDisplay();
//         }
//     }

//     async nextPage() {
//         if (this.currentPage < this.pageCount) {
//             this.currentPage++;
//             await this.loadPage();
//             this.updatePageDisplay();
//         }
//     }

//     updatePageDisplay() {
//         const pageInfo = document.getElementById('pageInfo');
//         if (pageInfo) {
//             pageInfo.textContent = `Page ${this.currentPage} of ${this.pageCount}`;
//         }

//         const prevBtn = document.getElementById('prevPageBtn');
//         const nextBtn = document.getElementById('nextPageBtn');

//         if (prevBtn) prevBtn.disabled = this.currentPage <= 1;
//         if (nextBtn) nextBtn.disabled = this.currentPage >= this.pageCount;
//     }

//     zoomIn() {
//         this.zoomLevel = Math.min(this.zoomLevel + 0.1, 3.0);
//         this.applyZoom();
//     }

//     zoomOut() {
//         this.zoomLevel = Math.max(this.zoomLevel - 0.1, 0.5);
//         this.applyZoom();
//     }

//     applyZoom() {
//         const pdfPage = document.getElementById('pdfPage');
//         if (pdfPage) {
//             pdfPage.style.transform = `scale(${this.zoomLevel})`;
//         }

//         const zoomDisplay = document.getElementById('zoomLevel');
//         if (zoomDisplay) {
//             zoomDisplay.textContent = `${Math.round(this.zoomLevel * 100)}%`;
//         }
//     }

//     // =====================
//     // Text Editing Functions
//     // =====================

//     handlePdfClick(e) {
//         if (this.isEditing) return;

//         const rect = e.currentTarget.getBoundingClientRect();
//         const x = e.clientX - rect.left;
//         const y = e.clientY - rect.top;

//         this.showEditModal(x, y, null, 'add');
//     }

//     handleRightClick(e) {
//         const rect = e.currentTarget.getBoundingClientRect();
//         const x = e.clientX - rect.left;
//         const y = e.clientY - rect.top;

//         // Check if clicking on existing text
//         const textId = this.getTextIdAtPosition(x, y);
//         if (textId) {
//             this.showEditModal(x, y, textId, 'edit');
//         }
//     }

//     showEditModal(x, y, textId, mode) {
//         const modal = document.getElementById('editModal');
//         if (!modal) return;

//         modal.style.display = 'block';
//         document.getElementById('editSessionId').value = this.sessionId;
//         document.getElementById('editPageNumber').value = this.currentPage;
//         document.getElementById('editX').value = x;
//         document.getElementById('editY').value = y;
//         document.getElementById('editTextId').value = textId || '';

//         // Update UI based on mode
//         const modalTitle = document.querySelector('.modal-title');
//         const saveBtn = document.getElementById('saveEditBtn');
//         const deleteBtn = document.getElementById('deleteTextBtn');

//         if (mode === 'add') {
//             modalTitle.textContent = 'Add Text';
//             saveBtn.textContent = 'Add Text';
//             deleteBtn.style.display = 'none';
//             document.getElementById('editText').value = '';
//         } else {
//             modalTitle.textContent = 'Edit Text';
//             saveBtn.textContent = 'Update Text';
//             deleteBtn.style.display = 'inline-block';
//             this.loadTextData(textId);
//         }

//         this.isEditing = true;
//         this.selectedTextId = textId;
//     }

//     async loadTextData(textId) {
//         try {
//             const response = await fetch(`/api/pdf/text/${this.sessionId}/${textId}`);
//             if (response.ok) {
//                 const textData = await response.json();
//                 document.getElementById('editText').value = textData.text || '';
//                 document.getElementById('editFontSize').value = textData.fontSize || 12;
//                 document.getElementById('editFontFamily').value = textData.fontFamily || 'helvetica';
//                 document.getElementById('editColor').value = textData.color || '#000000';
//             }
//         } catch (error) {
//             console.error('Error loading text data:', error);
//         }
//     }

//     async saveEdit() {
//         const request = {
//             sessionId: this.sessionId,
//             pageNumber: parseInt(document.getElementById('editPageNumber').value),
//             textId: document.getElementById('editTextId').value,
//             text: document.getElementById('editText').value,
//             x: parseFloat(document.getElementById('editX').value),
//             y: parseFloat(document.getElementById('editY').value),
//             fontSize: parseFloat(document.getElementById('editFontSize').value) || 12,
//             fontFamily: document.getElementById('editFontFamily').value || 'helvetica',
//             color: document.getElementById('editColor').value || '#000000'
//         };

//         try {
//             let url, method;
//             if (request.textId) {
//                 url = '/api/pdf/text/edit';
//                 method = 'PUT';
//             } else {
//                 url = '/api/pdf/text/add';
//                 method = 'POST';
//             }

//             const response = await fetch(url, {
//                 method: method,
//                 headers: {
//                     'Content-Type': 'application/json'
//                 },
//                 body: JSON.stringify(request)
//             });

//             if (response.ok) {
//                 this.cancelEdit();
//                 this.loadPage();
//                 this.showNotification('Text saved successfully!', 'success');
//             } else {
//                 const error = await response.text();
//                 this.showNotification(`Error: ${error}`, 'error');
//             }
//         } catch (error) {
//             console.error('Error saving text:', error);
//             this.showNotification('Error saving text', 'error');
//         }
//     }

//     async deleteText() {
//         if (!this.selectedTextId) return;

//         if (!confirm('Are you sure you want to delete this text?')) return;

//         try {
//             const response = await fetch('/api/pdf/text/delete', {
//                 method: 'DELETE',
//                 headers: {
//                     'Content-Type': 'application/json'
//                 },
//                 body: JSON.stringify({
//                     sessionId: this.sessionId,
//                     pageNumber: this.currentPage,
//                     textId: this.selectedTextId
//                 })
//             });

//             if (response.ok) {
//                 this.cancelEdit();
//                 this.loadPage();
//                 this.showNotification('Text deleted successfully!', 'success');
//             } else {
//                 const error = await response.text();
//                 this.showNotification(`Error: ${error}`, 'error');
//             }
//         } catch (error) {
//             console.error('Error deleting text:', error);
//             this.showNotification('Error deleting text', 'error');
//         }
//     }

//     cancelEdit() {
//         this.isEditing = false;
//         this.selectedTextId = null;
//         const modal = document.getElementById('editModal');
//         if (modal) {
//             modal.style.display = 'none';
//         }
//     }

//     getTextIdAtPosition(x, y) {
//         // This would need backend support to identify text elements at coordinates
//         // For now, returning null as placeholder
//         return null;
//     }

//     // =====================
//     // PDF Modification Operations
//     // =====================

//     async addPage() {
//         try {
//             const response = await fetch('/api/pdf/page/add', {
//                 method: 'POST',
//                 headers: {
//                     'Content-Type': 'application/json'
//                 },
//                 body: JSON.stringify({
//                     sessionId: this.sessionId,
//                     position: this.currentPage + 1,
//                     pageType: 'blank'
//                 })
//             });

//             if (response.ok) {
//                 const result = await response.json();
//                 this.pageCount = result.pageCount;
//                 this.currentPage = this.currentPage + 1;
//                 await this.loadPage();
//                 this.updatePageDisplay();
//                 this.showNotification('Page added successfully!', 'success');
//             } else {
//                 this.showNotification('Error adding page', 'error');
//             }
//         } catch (error) {
//             console.error('Error adding page:', error);
//             this.showNotification('Error adding page', 'error');
//         }
//     }

//     async deletePage() {
//         if (this.pageCount <= 1) {
//             this.showNotification('Cannot delete the last page', 'warning');
//             return;
//         }

//         if (!confirm('Are you sure you want to delete this page?')) return;

//         try {
//             const response = await fetch('/api/pdf/page/delete', {
//                 method: 'DELETE',
//                 headers: {
//                     'Content-Type': 'application/json'
//                 },
//                 body: JSON.stringify({
//                     sessionId: this.sessionId,
//                     pageNumber: this.currentPage
//                 })
//             });

//             if (response.ok) {
//                 const result = await response.json();
//                 this.pageCount = result.pageCount;

//                 if (this.currentPage > this.pageCount) {
//                     this.currentPage = this.pageCount;
//                 }

//                 await this.loadPage();
//                 this.updatePageDisplay();
//                 this.showNotification('Page deleted successfully!', 'success');
//             } else {
//                 this.showNotification('Error deleting page', 'error');
//             }
//         } catch (error) {
//             console.error('Error deleting page:', error);
//             this.showNotification('Error deleting page', 'error');
//         }
//     }

//     async rotatePage(degrees) {
//         try {
//             const response = await fetch('/api/pdf/rotate', {
//                 method: 'POST',
//                 headers: {
//                     'Content-Type': 'application/json'
//                 },
//                 body: JSON.stringify({
//                     sessionId: this.sessionId,
//                     pageNumber: this.currentPage,
//                     degrees: degrees
//                 })
//             });

//             if (response.ok) {
//                 await this.loadPage();
//                 this.showNotification(`Page rotated ${degrees} degrees`, 'success');
//             } else {
//                 this.showNotification('Error rotating page', 'error');
//             }
//         } catch (error) {
//             console.error('Error rotating page:', error);
//             this.showNotification('Error rotating page', 'error');
//         }
//     }

//     async compressPdf() {
//         try {
//             const response = await fetch(`/api/pdf/compress/${this.sessionId}`, {
//                 method: 'POST'
//             });

//             if (response.ok) {
//                 this.showNotification('PDF compressed successfully!', 'success');
//                 await this.loadPage(); // Refresh the view
//             } else {
//                 this.showNotification('Error compressing PDF', 'error');
//             }
//         } catch (error) {
//             console.error('Error compressing PDF:', error);
//             this.showNotification('Error compressing PDF', 'error');
//         }
//     }

//     // =====================
//     // Merge & Split Operations
//     // =====================

//     showMergeModal() {
//         const modal = document.getElementById('mergeModal');
//         if (modal) {
//             modal.style.display = 'block';
//         }
//     }

//     showSplitModal() {
//         const modal = document.getElementById('splitModal');
//         if (modal) {
//             modal.style.display = 'block';
//             document.getElementById('splitAtPage').value = this.currentPage;
//         }
//     }

//     async handleMergeFile(e) {
//         const file = e.target.files[0];
//         if (!file) return;

//         const formData = new FormData();
//         formData.append('file', file);
//         formData.append('sessionId', this.sessionId);

//         try {
//             const response = await fetch('/api/pdf/merge', {
//                 method: 'POST',
//                 body: formData
//             });

//             if (response.ok) {
//                 const result = await response.json();
//                 this.pageCount = result.pageCount;
//                 this.updatePageDisplay();
//                 this.showNotification('PDF merged successfully!', 'success');
//                 this.closeMergeModal();
//             } else {
//                 this.showNotification('Error merging PDF', 'error');
//             }
//         } catch (error) {
//             console.error('Error merging PDF:', error);
//             this.showNotification('Error merging PDF', 'error');
//         }
//     }

//     async splitPdf() {
//         const splitAtPage = parseInt(document.getElementById('splitAtPage').value);

//         if (splitAtPage < 1 || splitAtPage > this.pageCount) {
//             this.showNotification('Invalid page number', 'error');
//             return;
//         }

//         try {
//             const response = await fetch('/api/pdf/split', {
//                 method: 'POST',
//                 headers: {
//                     'Content-Type': 'application/json'
//                 },
//                 body: JSON.stringify({
//                     sessionId: this.sessionId,
//                     splitAtPage: splitAtPage
//                 })
//             });

//             if (response.ok) {
//                 const result = await response.json();
//                 this.showNotification(`PDF split successfully! New session ID: ${result.newSessionId}`, 'success');
//                 this.updatePageDisplay();
//                 this.closeSplitModal();
//             } else {
//                 this.showNotification('Error splitting PDF', 'error');
//             }
//         } catch (error) {
//             console.error('Error splitting PDF:', error);
//             this.showNotification('Error splitting PDF', 'error');
//         }
//     }

//     closeMergeModal() {
//         const modal = document.getElementById('mergeModal');
//         if (modal) {
//             modal.style.display = 'none';
//             document.getElementById('mergeFileInput').value = '';
//         }
//     }

//     closeSplitModal() {
//         const modal = document.getElementById('splitModal');
//         if (modal) {
//             modal.style.display = 'none';
//         }
//     }

//     // =====================
//     // File Operations
//     // =====================

//     async saveDocument() {
//         try {
//             const response = await fetch(`/api/pdf/save/${this.sessionId}`, {
//                 method: 'POST'
//             });

//             if (response.ok) {
//                 this.showNotification('Document saved successfully!', 'success');
//             } else {
//                 this.showNotification('Error saving document', 'error');
//             }
//         } catch (error) {
//             console.error('Error saving document:', error);
//             this.showNotification('Error saving document', 'error');
//         }
//     }

//     async downloadPdf() {
//         try {
//             const response = await fetch(`/api/pdf/download/${this.sessionId}`);
//             if (response.ok) {
//                 const blob = await response.blob();
//                 const url = window.URL.createObjectURL(blob);
//                 const a = document.createElement('a');
//                 a.href = url;
//                 a.download = `document_${new Date().getTime()}.pdf`;
//                 document.body.appendChild(a);
//                 a.click();
//                 document.body.removeChild(a);
//                 window.URL.revokeObjectURL(url);
//                 this.showNotification('PDF downloaded successfully!', 'success');
//             } else {
//                 this.showNotification('Error downloading PDF', 'error');
//             }
//         } catch (error) {
//             console.error('Error downloading PDF:', error);
//             this.showNotification('Error downloading PDF', 'error');
//         }
//     }

//     // =====================
//     // Utility Functions
//     // =====================

//     showNotification(message, type = 'info') {
//         const notification = document.createElement('div');
//         notification.className = `notification ${type}`;
//         notification.textContent = message;

//         notification.style.cssText = `
//             position: fixed;
//             top: 20px;
//             right: 20px;
//             padding: 12px 24px;
//             border-radius: 4px;
//             color: white;
//             font-weight: bold;
//             z-index: 10000;
//             min-width: 250px;
//             box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
//         `;

//         switch (type) {
//             case 'success':
//                 notification.style.backgroundColor = '#4CAF50';
//                 break;
//             case 'error':
//                 notification.style.backgroundColor = '#f44336';
//                 break;
//             case 'warning':
//                 notification.style.backgroundColor = '#ff9800';
//                 break;
//             default:
//                 notification.style.backgroundColor = '#2196F3';
//         }

//         document.body.appendChild(notification);

//         setTimeout(() => {
//             notification.remove();
//         }, 5000);
//     }
// }

// // PDF Upload Manager (handles file uploads and session creation)
// class PdfUploadManager {
//     constructor() {
//         this.maxFileSize = 50 * 1024 * 1024; // 50MB
//         this.allowedTypes = ['application/pdf'];
//         this.uploadInProgress = false;
//         this.init();
//     }

//     init() {
//         this.bindEvents();
//         this.createProgressBar();
//     }

//     bindEvents() {
//         // File input change event
//         const fileInput = document.getElementById('pdfUpload');
//         if (fileInput) {
//             fileInput.addEventListener('change', (e) => this.handleFileSelect(e));
//         }

//         // Upload button
//         const uploadBtn = document.getElementById('uploadBtn');
//         if (uploadBtn) {
//             uploadBtn.addEventListener('click', () => this.initiateUpload());
//         }
//     }

//     createProgressBar() {
//         // Create progress bar if it doesn't exist
//         if (!document.getElementById('uploadProgress')) {
//             const progressContainer = document.createElement('div');
//             progressContainer.id = 'uploadProgressContainer';
//             progressContainer.style.cssText = `
//                 display: none;
//                 margin: 20px 0;
//                 padding: 10px;
//                 border: 1px solid #ddd;
//                 border-radius: 5px;
//                 background-color: #f9f9f9;
//             `;

//             const progressBar = document.createElement('div');
//             progressBar.id = 'uploadProgress';
//             progressBar.style.cssText = `
//                 width: 100%;
//                 height: 20px;
//                 background-color: #e0e0e0;
//                 border-radius: 10px;
//                 overflow: hidden;
//             `;

//             const progressFill = document.createElement('div');
//             progressFill.id = 'uploadProgressFill';
//             progressFill.style.cssText = `
//                 height: 100%;
//                 background-color: #4CAF50;
//                 width: 0%;
//                 transition: width 0.3s ease;
//             `;

//             const progressText = document.createElement('div');
//             progressText.id = 'uploadProgressText';
//             progressText.style.cssText = `
//                 text-align: center;
//                 margin-top: 5px;
//                 font-size: 14px;
//                 color: #333;
//             `;

//             progressBar.appendChild(progressFill);
//             progressContainer.appendChild(progressBar);
//             progressContainer.appendChild(progressText);

//             // Insert after file input
//             const fileInput = document.getElementById('pdfUpload');
//             if (fileInput && fileInput.parentNode) {
//                 fileInput.parentNode.insertBefore(progressContainer, fileInput.nextSibling);
//             }
//         }
//     }

//     handleFileSelect(event) {
//         const file = event.target.files[0];
//         if (file) {
//             this.validateAndPrepareFile(file);
//             this.initiateUpload();
//         }
//     }

//     validateAndPrepareFile(file) {
//         // Reset any previous states
//         this.hideProgress();
//         this.clearMessages();

//         // Validate file type
//         if (!this.allowedTypes.includes(file.type)) {
//             this.showError('Please select a valid PDF file.');
//             return;
//         }

//         // Validate file size
//         if (file.size > this.maxFileSize) {
//             this.showError(`File size too large. Maximum size is ${this.formatFileSize(this.maxFileSize)}.`);
//             return;
//         }

//         // Show file info
//         this.showFileInfo(file);
        
//         // Enable upload button
//         const uploadBtn = document.getElementById('uploadBtn');
//         if (uploadBtn) {
//             uploadBtn.disabled = false;
//             uploadBtn.textContent = 'Upload PDF';
//         }

//         // Store file for upload
//         this.selectedFile = file;
//     }

//     showFileInfo(file) {
//         const fileInfo = document.getElementById('fileInfo');
//         if (fileInfo) {
//             fileInfo.innerHTML = `
//                 <strong>Selected File:</strong> ${file.name}<br>
//                 <strong>Size:</strong> ${this.formatFileSize(file.size)}<br>
//                 <strong>Type:</strong> ${file.type}
//             `;
//             fileInfo.style.display = 'block';
//         }
//     }

//     async initiateUpload() {
//         if (!this.selectedFile) {
//             this.showError('Please select a PDF file first.');
//             return;
//         }

//         if (this.uploadInProgress) {
//             this.showWarning('Upload already in progress. Please wait.');
//             return;
//         }

//         await this.uploadFile(this.selectedFile);
//     }

//     async uploadFile(file) {
//         this.uploadInProgress = true;
//         this.showProgress();
//         this.updateProgress(0, 'Preparing upload...');

//         const formData = new FormData();
//         formData.append('file', file);

//         try {
//             const response = await fetch('/api/upload', {
//                 method: 'POST',
//                 body: formData
//             });

//             this.updateProgress(90, 'Processing file...');

//             if (!response.ok) {
//                 const errorText = await response.text();
//                 throw new Error(`Upload failed: ${response.status} ${response.statusText} - ${errorText}`);
//             }

//             const result = await response.json();
//             this.updateProgress(100, 'Upload complete!');

//             if (result.success) {
//                 this.handleUploadSuccess(result);
//             } else {
//                 throw new Error(result.error || 'Upload failed for unknown reason');
//             }

//         } catch (error) {
//             console.error('Upload error:', error);
//             this.handleUploadError(error);
//         } finally {
//             this.uploadInProgress = false;
//             setTimeout(() => this.hideProgress(), 2000);
//         }
//     }

//     handleUploadSuccess(result) {
//         this.showSuccess('Upload successful!');
//         const sessionId = result.sessionId;
//         const pdfUrl = `/api/editor/session/${sessionId}/pdf`;
//         document.getElementById('pdfCanvas').style.display = 'block';
//         document.getElementById('pdfPlaceholder').style.display = 'none';
//         console.log("Loading PDF from", pdfUrl);
//         pdfjsLib.getDocument(pdfUrl).promise.then(pdfDoc => {
//             console.log("PDF loaded", pdfDoc);
//             pdfDoc.getPage(1).then(page => {
//                 console.log("Rendering page 1");
//                 const canvas = document.getElementById('pdfCanvas');
//                 const context = canvas.getContext('2d');
//                 const viewport = page.getViewport({ scale: 1.0 });
//                 canvas.height = viewport.height;
//                 canvas.width = viewport.width;
//                 page.render({ canvasContext: context, viewport: viewport });
//             });
//         }).catch(err => {
//             console.error("PDF.js error", err);
//             this.showError('Failed to load PDF: ' + err.message);
//         });
//     }

//     handleUploadError(error) {
//         this.showError(`Upload failed: ${error.message}`);
//         this.updateProgress(0, 'Upload failed');
//     }

//     showProgress() {
//         const progressContainer = document.getElementById('uploadProgressContainer');
//         if (progressContainer) {
//             progressContainer.style.display = 'block';
//         }
//     }

//     hideProgress() {
//         const progressContainer = document.getElementById('uploadProgressContainer');
//         if (progressContainer) {
//             progressContainer.style.display = 'none';
//         }
//     }

//     updateProgress(percentage, message) {
//         const progressFill = document.getElementById('uploadProgressFill');
//         const progressText = document.getElementById('uploadProgressText');
        
//         if (progressFill) {
//             progressFill.style.width = `${percentage}%`;
//         }
        
//         if (progressText) {
//             progressText.textContent = `${message} (${percentage}%)`;
//         }
//     }

//     showSuccess(message) {
//         this.showMessage(message, 'success');
//     }

//     showError(message) {
//         this.showMessage(message, 'error');
//     }

//     showWarning(message) {
//         this.showMessage(message, 'warning');
//     }

//     showMessage(message, type = 'info') {
//         this.clearMessages();

//         const messageDiv = document.createElement('div');
//         messageDiv.className = `upload-message ${type}`;
//         messageDiv.textContent = message;
//         messageDiv.style.cssText = `
//             padding: 10px;
//             margin: 10px 0;
//             border-radius: 4px;
//             font-weight: bold;
//             text-align: center;
//         `;

//         switch (type) {
//             case 'success':
//                 messageDiv.style.backgroundColor = '#d4edda';
//                 messageDiv.style.color = '#155724';
//                 messageDiv.style.border = '1px solid #c3e6cb';
//                 break;
//             case 'error':
//                 messageDiv.style.backgroundColor = '#f8d7da';
//                 messageDiv.style.color = '#721c24';
//                 messageDiv.style.border = '1px solid #f5c6cb';
//                 break;
//             case 'warning':
//                 messageDiv.style.backgroundColor = '#fff3cd';
//                 messageDiv.style.color = '#856404';
//                 messageDiv.style.border = '1px solid #ffeaa7';
//                 break;
//             default:
//                 messageDiv.style.backgroundColor = '#d1ecf1';
//                 messageDiv.style.color = '#0c5460';
//                 messageDiv.style.border = '1px solid #bee5eb';
//         }

//         // Insert message after file input
//         const fileInput = document.getElementById('pdfUpload');
//         if (fileInput && fileInput.parentNode) {
//             fileInput.parentNode.insertBefore(messageDiv, fileInput.nextSibling);
//         }

//         // Auto-remove success messages after 5 seconds
//         if (type === 'success') {
//             setTimeout(() => {
//                 if (messageDiv.parentNode) {
//                     messageDiv.parentNode.removeChild(messageDiv);
//                 }
//             }, 5000);
//         }
//     }

//     clearMessages() {
//         const messages = document.querySelectorAll('.upload-message');
//         messages.forEach(msg => {
//             if (msg.parentNode) {
//                 msg.parentNode.removeChild(msg);
//             }
//         });
//     }

//     formatFileSize(bytes) {
//         if (bytes === 0) return '0 Bytes';
//         const k = 1024;
//         const sizes = ['Bytes', 'KB', 'MB', 'GB'];
//         const i = Math.floor(Math.log(bytes) / Math.log(k));
//         return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
//     }
// }

// // Initialize when DOM is ready
// document.addEventListener('DOMContentLoaded', function() {
//     // Initialize upload manager
//     window.pdfUploadManager = new PdfUploadManager();
    
//     // Check if we have a session ID in the URL or storage
//     const urlParams = new URLSearchParams(window.location.search);
//     const sessionId = urlParams.get('sessionId') || sessionStorage.getItem('pdfSessionId');
    
//     if (sessionId) {
//         // Initialize editor with existing session
//         window.pdfEditor = new PdfEditor(sessionId, 1);
//         document.getElementById('pdfContainer').style.display = 'block';
//     }
// });

document.getElementById('uploadForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    const fileInput = document.getElementById('pdfFile');
    const file = fileInput.files[0];
    const message = document.getElementById('message');
    if (!file) {
        message.textContent = 'Please select a PDF file.';
        return;
    }
    if (file.type !== 'application/pdf') {
        message.textContent = 'Only PDF files are allowed.';
        return;
    }
    if (file.size > 50 * 1024 * 1024) { // 50MB limit
        message.textContent = 'File is too large (max 50MB).';
        return;
    }

    const formData = new FormData();
    formData.append('file', file);

    message.textContent = 'Uploading...';

    try {
        const response = await fetch('/api/upload', {
            method: 'POST',
            body: formData
        });
        if (!response.ok) throw new Error('Upload failed');
        const { fileId } = await response.json();
        message.textContent = 'Upload successful! Loading PDF...';
        await loadPdf(`/api/pdf/${fileId}`);
    } catch (err) {
        message.textContent = 'Error: ' + err.message;
    }
});

async function loadPdf(url) {
    const canvas = document.getElementById('pdfCanvas');
    const message = document.getElementById('message');
    try {
        const loadingTask = pdfjsLib.getDocument(url);
        const pdf = await loadingTask.promise;
        const page = await pdf.getPage(1);
        const viewport = page.getViewport({ scale: 2.0 }); // High quality
        canvas.height = viewport.height;
        canvas.width = viewport.width;
        canvas.style.display = 'block';
        await page.render({ canvasContext: canvas.getContext('2d'), viewport }).promise;
        message.textContent = '';
    } catch (err) {
        canvas.style.display = 'none';
        message.textContent = 'Failed to render PDF: ' + err.message;
    }
}

