class PdfEditor {
    constructor() {
        this.pdfDoc = null;
        this.pageNum = 1;
        this.pageRendering = false;
        this.pageNumPending = null;
        this.scale = 1.0;
        this.canvas = document.getElementById('pdfCanvas');
        this.ctx = this.canvas.getContext('2d');
        this.signatureCanvas = document.getElementById('signatureCanvas');
        this.signaturePad = new SignaturePad(this.signatureCanvas);

        this.init();
    }

    init() {
        // Load PDF from URL parameter or default
        const urlParams = new URLSearchParams(window.location.search);
        const pdfFile = urlParams.get('file') || '/sample.pdf';

        // Initialize PDF.js
        pdfjsLib.getDocument(pdfFile).promise.then(pdfDoc_ => {
            this.pdfDoc = pdfDoc_;
            document.getElementById('pageCount').textContent = `of ${this.pdfDoc.numPages}`;

            // Render the first page
            this.renderPage(this.pageNum);

            // Generate thumbnails
            this.generateThumbnails();
        });

        // Event listeners
        document.getElementById('prevPage').addEventListener('click', () => {
            if (this.pageNum <= 1) return;
            this.pageNum--;
            this.queueRenderPage(this.pageNum);
        });

        document.getElementById('nextPage').addEventListener('click', () => {
            if (this.pageNum >= this.pdfDoc.numPages) return;
            this.pageNum++;
            this.queueRenderPage(this.pageNum);
        });

        document.getElementById('zoomControl').addEventListener('input', (e) => {
            this.scale = e.target.value / 100;
            document.getElementById('zoomValue').textContent = `${e.target.value}%`;
            this.queueRenderPage(this.pageNum);
        });

        // Initialize signature modal
        const signatureModal = new bootstrap.Modal(document.getElementById('signatureModal'));
        document.getElementById('signatureTool').addEventListener('click', () => {
            signatureModal.show();
        });

        document.getElementById('clearSignature').addEventListener('click', () => {
            this.signaturePad.clear();
        });

        document.getElementById('saveSignature').addEventListener('click', () => {
            if (!this.signaturePad.isEmpty()) {
                // Add signature to PDF
                this.addSignatureToPdf(this.signaturePad.toDataURL());
                signatureModal.hide();
                this.signaturePad.clear();
            }
        });
    }

    queueRenderPage(num) {
        if (this.pageRendering) {
            this.pageNumPending = num;
        } else {
            this.renderPage(num);
        }
    }

    renderPage(num) {
        this.pageRendering = true;
        this.pdfDoc.getPage(num).then(page => {
            const viewport = page.getViewport({ scale: this.scale });
            this.canvas.height = viewport.height;
            this.canvas.width = viewport.width;

            const renderContext = {
                canvasContext: this.ctx,
                viewport: viewport
            };

            const renderTask = page.render(renderContext);
            renderTask.promise.then(() => {
                this.pageRendering = false;
                if (this.pageNumPending !== null) {
                    this.renderPage(this.pageNumPending);
                    this.pageNumPending = null;
                }
            });

            document.getElementById('pageNum').textContent = `Page: ${num}`;
            this.highlightThumbnail(num);
        });
    }

    generateThumbnails() {
        const container = document.getElementById('pageThumbnails');
        container.innerHTML = '';

        for (let i = 1; i <= this.pdfDoc.numPages; i++) {
            const thumbItem = document.createElement('button');
            thumbItem.className = 'list-group-item list-group-item-action';
            thumbItem.innerHTML = `
                <div class="d-flex align-items-center">
                    <span class="badge bg-primary me-2">${i}</span>
                    <canvas class="thumbnail-canvas" data-page="${i}"></canvas>
                </div>
            `;

            thumbItem.addEventListener('click', () => {
                this.pageNum = i;
                this.queueRenderPage(i);
            });

            container.appendChild(thumbItem);

            // Render thumbnail
            this.renderThumbnail(i);
        }
    }

    renderThumbnail(pageNum) {
        this.pdfDoc.getPage(pageNum).then(page => {
            const viewport = page.getViewport(0.2);
            const canvas = document.querySelector(`.thumbnail-canvas[data-page="${pageNum}"]`);
            const context = canvas.getContext('2d');
            canvas.height = viewport.height;
            canvas.width = viewport.width;

            page.render({
                canvasContext: context,
                viewport: viewport
            });
        });
    }

    highlightThumbnail(pageNum) {
        document.querySelectorAll('#pageThumbnails button').forEach(btn => {
            btn.classList.remove('active');
        });
        const activeThumb = document.querySelector(`#pageThumbnails button:nth-child(${pageNum})`);
        if (activeThumb) activeThumb.classList.add('active');
    }

    addSignatureToPdf(signatureDataUrl) {
        // This would need integration with your backend to add the signature to the PDF
        console.log('Signature to add:', signatureDataUrl);
        // In a real implementation, you would send this to your backend
        // to add to the PDF using PDFBox or iText
    }
}

// Initialize editor when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    new PdfEditor();
});