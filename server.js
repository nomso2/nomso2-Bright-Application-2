const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 3000;
const PUBLIC_DIR = path.join(__dirname, 'public');

const server = http.createServer((req, res) => {
    console.log(`[${new Date().toISOString()}] ${req.method} ${req.url}`);

    let reqPath = req.url.split('?')[0];
    if (reqPath === '/' || reqPath === '') {
        reqPath = '/index.html';
    }

    const filePath = path.join(PUBLIC_DIR, reqPath);

    // Security check to avoid directory traversal
    if (!filePath.startsWith(PUBLIC_DIR)) {
        res.writeHead(403, { 'Content-Type': 'text/plain' });
        res.end('Forbidden');
        return;
    }

    fs.stat(filePath, (err, stats) => {
        if (err || !stats.isFile()) {
            res.writeHead(404, { 'Content-Type': 'text/plain' });
            res.end('File Not Found');
            return;
        }

        const ext = path.extname(filePath).toLowerCase();
        let contentType = 'application/octet-stream';
        let headers = {
            'Content-Length': stats.size,
            'Cache-Control': 'no-cache'
        };

        if (ext === '.apk') {
            contentType = 'application/vnd.android.package-archive';
            headers['Content-Disposition'] = 'attachment; filename="bright-app-debug.apk"';
        } else if (ext === '.html') {
            contentType = 'text/html; charset=UTF-8';
        } else if (ext === '.css') {
            contentType = 'text/css';
        } else if (ext === '.js') {
            contentType = 'application/javascript';
        } else if (ext === '.png') {
            contentType = 'image/png';
        } else if (ext === '.svg') {
            contentType = 'image/svg+xml';
        }

        headers['Content-Type'] = contentType;

        if (req.method === 'HEAD') {
            res.writeHead(200, headers);
            res.end();
            return;
        }

        res.writeHead(200, headers);
        const stream = fs.createReadStream(filePath);
        stream.pipe(res);
    });
});

server.listen(PORT, '0.0.0.0', () => {
    console.log(`Bright Web & APK Server listening on http://0.0.0.0:${PORT}`);
});
