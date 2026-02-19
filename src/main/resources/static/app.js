// Base URL for all API requests.
// Empty string means relative URLs, which works when the frontend is served by the same
// Spring Boot process (the default setup). To point at a separate backend (e.g. during
// local frontend development or a split deployment), set this to the full origin:
//   const API_BASE = 'http://localhost:8080';
const API_BASE = '';

// Global error handler for uncaught errors
window.addEventListener('error', (event) => {
    console.error('Uncaught error:', event.error);
    showToast(`Unexpected error: ${event.error?.message || 'Unknown error'}`, 'error', 10000);
});

// Global handler for unhandled promise rejections
window.addEventListener('unhandledrejection', (event) => {
    console.error('Unhandled promise rejection:', event.reason);
    showToast(`Promise rejection: ${event.reason?.message || event.reason || 'Unknown error'}`, 'error', 10000);
});

// Load nodes on page load
window.addEventListener('DOMContentLoaded', async () => {
    await loadNodes();
    await checkTrainingStatus();
});

// Load available nodes
async function loadNodes() {
    try {
        const response = await fetch(`${API_BASE}/api/nodes/with-dataset`);
        if (!response.ok) {
            // If endpoint doesn't exist, fallback to old endpoint
            const fallbackResponse = await fetch(`${API_BASE}/api/nodes`);
            if (!fallbackResponse.ok) {
                showToast('Could not load nodes from server. Using default nodes.', 'warning', 5000);
                populateNodesDropdown(['Atlanta', 'Boston', 'Chicago', 'Dallas', 'Denver',
                    'Houston', 'Las Vegas', 'Los Angeles', 'Miami', 'New York',
                    'Philadelphia', 'Phoenix', 'San Francisco', 'Seattle', 'Washington']);
                return;
            }
            const nodes = await fallbackResponse.json();
            populateNodesDropdown(nodes);
            return;
        }
        const nodes = await response.json();
        populateNodesDropdownWithDatasets(nodes);
    } catch (error) {
        console.error('Error loading nodes:', error);
        const errorMsg = `Failed to load nodes from server: ${error.message}. Using default nodes.`;
        showToast(errorMsg, 'error', 10000);
        // Use fallback nodes
        populateNodesDropdown(['Atlanta', 'Boston', 'Chicago', 'Dallas', 'Denver',
            'Houston', 'Las Vegas', 'Los Angeles', 'Miami', 'New York',
            'Philadelphia', 'Phoenix', 'San Francisco', 'Seattle', 'Washington']);
    }
}

function populateNodesDropdown(nodes) {
    const startSelect = document.getElementById('startNode');
    const endSelect = document.getElementById('endNode');

    startSelect.innerHTML = '<option value="">-- Select Start Node --</option>';
    endSelect.innerHTML = '<option value="">-- Select End Node --</option>';

    nodes.forEach(node => {
        const nodeName = typeof node === 'string' ? node : node.name;
        startSelect.innerHTML += `<option value="${nodeName}">${nodeName}</option>`;
        endSelect.innerHTML += `<option value="${nodeName}">${nodeName}</option>`;
    });
}

function populateNodesDropdownWithDatasets(nodes) {
    const startSelect = document.getElementById('startNode');
    const endSelect = document.getElementById('endNode');

    // Group nodes by dataset
    const trainingNodes = nodes.filter(n => n.datasetType === 'training');
    const testingNodes = nodes.filter(n => n.datasetType === 'testing');

    // Populate start select
    startSelect.innerHTML = '<option value="">-- Select Start Node --</option>';
    if (trainingNodes.length > 0) {
        startSelect.innerHTML += '<optgroup label="📚 Training Dataset">';
        trainingNodes.forEach(node => {
            startSelect.innerHTML += `<option value="${node.name}" data-dataset="${node.datasetType}">${node.name}</option>`;
        });
        startSelect.innerHTML += '</optgroup>';
    }
    if (testingNodes.length > 0) {
        startSelect.innerHTML += '<optgroup label="🧪 Testing Dataset">';
        testingNodes.forEach(node => {
            startSelect.innerHTML += `<option value="${node.name}" data-dataset="${node.datasetType}">${node.name}</option>`;
        });
        startSelect.innerHTML += '</optgroup>';
    }

    // Populate end select
    endSelect.innerHTML = '<option value="">-- Select End Node --</option>';
    if (trainingNodes.length > 0) {
        endSelect.innerHTML += '<optgroup label="📚 Training Dataset">';
        trainingNodes.forEach(node => {
            endSelect.innerHTML += `<option value="${node.name}" data-dataset="${node.datasetType}">${node.name}</option>`;
        });
        endSelect.innerHTML += '</optgroup>';
    }
    if (testingNodes.length > 0) {
        endSelect.innerHTML += '<optgroup label="🧪 Testing Dataset">';
        testingNodes.forEach(node => {
            endSelect.innerHTML += `<option value="${node.name}" data-dataset="${node.datasetType}">${node.name}</option>`;
        });
        endSelect.innerHTML += '</optgroup>';
    }

    // Store nodes globally for validation
    window.nodeDatasets = nodes.reduce((acc, node) => {
        acc[node.name] = node.datasetType;
        return acc;
    }, {});

    // Add change listeners to validate dataset consistency
    startSelect.addEventListener('change', validateDatasetConsistency);
    endSelect.addEventListener('change', validateDatasetConsistency);
}

function validateDatasetConsistency() {
    const startNode = document.getElementById('startNode').value;
    const endNode = document.getElementById('endNode').value;

    if (!startNode || !endNode || !window.nodeDatasets) return;

    const startDataset = window.nodeDatasets[startNode];
    const endDataset = window.nodeDatasets[endNode];

    if (startDataset !== endDataset) {
        showToast(
            `⚠️ Warning: ${startNode} is in ${startDataset} dataset, but ${endNode} is in ${endDataset} dataset. There is no path between different datasets!`,
            'warning',
            8000
        );
    }
}

// Train the model with real-time progress
async function trainModel() {
    const episodes = document.getElementById('episodes').value;
    const btn = document.querySelector('.btn-primary');
    const btnText = document.getElementById('trainBtnText');
    const originalText = btnText.innerHTML;

    // Show progress UI
    document.getElementById('trainingProgress').style.display = 'block';
    document.getElementById('progressBar').style.width = '0%';
    document.getElementById('progressPercent').textContent = '0%';
    document.getElementById('progressText').textContent = `0 / ${episodes} episodes (0%)`;
    document.getElementById('currentLoss').textContent = '-';

    try {
        btnText.innerHTML = 'Training... <span class="loading"></span>';
        btn.disabled = true;

        showStatus('trainingStatus', 'Connecting to training stream...', 'info');

        // Use Server-Sent Events for real-time progress
        const eventSource = new EventSource(`${API_BASE}/api/comparison/train-stream?episodes=${episodes}`);

        eventSource.addEventListener('progress', (event) => {
            const progress = JSON.parse(event.data);

            // Update progress bar
            const percent = progress.percentComplete;
            document.getElementById('progressBar').style.width = `${percent}%`;
            document.getElementById('progressPercent').textContent = `${percent}%`;
            document.getElementById('progressText').textContent =
                `${progress.currentEpisode} / ${progress.totalEpisodes} episodes (${percent}%)`;
            document.getElementById('currentLoss').textContent = progress.averageLoss.toFixed(4);

            showStatus('trainingStatus',
                `Training in progress: ${progress.currentEpisode}/${progress.totalEpisodes} episodes`, 'info');
        });

        eventSource.addEventListener('complete', (event) => {
            const result = JSON.parse(event.data);
            eventSource.close();

            // Show completion
            document.getElementById('progressBar').style.width = '100%';
            document.getElementById('progressPercent').textContent = '100%';

            showStatus('trainingStatus',
                `✅ Training completed! Total episodes: ${result.totalEpisodes}, Average Loss: ${result.averageLoss.toFixed(4)}`,
                'success');

            showToast(`Training completed successfully in ${result.totalEpisodes} episodes!`, 'success', 5000);

            // Show training info with all fields including training time
            displayTrainingInfo({
                isTrained: true,
                totalEpisodes: result.totalEpisodes,
                averageLoss: result.averageLoss,
                trainingTimeMillis: result.trainingTimeMillis,
                status: result.status
            });

            btnText.innerHTML = originalText;
            btn.disabled = false;
        });

        eventSource.onerror = (error) => {
            console.error('SSE Error:', error);
            eventSource.close();

            showStatus('trainingStatus', '❌ Training failed or connection lost. Please try again.', 'error');
            showToast('Training connection failed. Check server logs.', 'error', 8000);

            btnText.innerHTML = originalText;
            btn.disabled = false;
        };

    } catch (error) {
        console.error('Error:', error);
        showStatus('trainingStatus', `❌ Error: ${error.message}`, 'error');
        showToast(`Training error: ${error.message}`, 'error', 8000);

        btnText.innerHTML = originalText;
        btn.disabled = false;
    }
}

// Check training status
async function checkTrainingStatus() {
    try {
        const response = await fetch(`${API_BASE}/api/comparison/training-status`);
        if (!response.ok) {
            throw new Error(`Failed to check training status: ${response.status} ${response.statusText}`);
        }
        const data = await response.json();

        if (data.isTrained) {
            showStatus('trainingStatus', '✅ Model is trained and ready!', 'success');
        } else {
            showStatus('trainingStatus', '⚠️ Model not trained yet. Click "Start Training" to begin.', 'info');
        }
    } catch (error) {
        console.error('Error checking status:', error);
        const errorMsg = `Failed to check training status: ${error.message}`;
        showToast(errorMsg, 'error', 7000);
        showStatus('trainingStatus', '❌ Could not check training status', 'error');
    }
}

function displayTrainingInfo(data) {
    const infoDiv = document.getElementById('trainingInfo');
    infoDiv.style.display = 'grid';

    // Handle trainingTimeMillis safely
    let trainingTimeDisplay = 'N/A';
    if (data.trainingTimeMillis && !isNaN(data.trainingTimeMillis) && data.trainingTimeMillis > 0) {
        trainingTimeDisplay = (data.trainingTimeMillis / 1000).toFixed(1) + 's';
    }

    infoDiv.innerHTML = `
        <div class="info-box">
            <div class="info-box-label">Episodes</div>
            <div class="info-box-value">${data.totalEpisodes || 0}</div>
        </div>
        <div class="info-box">
            <div class="info-box-label">Training Time</div>
            <div class="info-box-value">${trainingTimeDisplay}</div>
        </div>
        <div class="info-box">
            <div class="info-box-label">Avg Loss</div>
            <div class="info-box-value">${(data.averageLoss || 0).toFixed(4)}</div>
        </div>
    `;
}

// Check database health
async function checkDatabaseHealth() {
    const btn = document.querySelector('#healthBtnText');
    const originalText = btn.innerHTML;

    try {
        btn.innerHTML = 'Checking... <span class="loading"></span>';

        const response = await fetch(`${API_BASE}/api/comparison/health`);

        if (!response.ok) {
            throw new Error(`Health check failed: ${response.status}`);
        }

        const data = await response.json();
        console.log('Database health:', data);

        // Build health report
        let healthReport = `🏥 DATABASE HEALTH CHECK\n\n`;
        healthReport += `Status: ${data.status}\n`;
        healthReport += `Total Nodes: ${data.totalNodes}\n`;
        healthReport += `Training Nodes: ${data.trainingNodes}\n`;
        healthReport += `Testing Nodes: ${data.testingNodes}\n`;
        healthReport += `Sample Node: ${data.sampleNodeName} (${data.sampleNodeEdges} edges)\n`;
        healthReport += `Path Test: ${data.pathTest}\n`;

        if (data.warning) {
            healthReport += `\n${data.warning}`;
        }

        if (data.status === 'HEALTHY') {
            showToast('✅ Database is healthy!', 'success', 5000);
        } else if (data.status === 'UNHEALTHY') {
            showToast('❌ Database is UNHEALTHY!', 'error', 10000);
            healthReport += `\n\n⚠️ ACTION REQUIRED:\n`;
            healthReport += `1. Open http://localhost:7474 (Neo4j Browser)\n`;
            healthReport += `2. Run: MATCH (n) DETACH DELETE n;\n`;
            healthReport += `3. Copy/paste entire neo4j/import/init.cypher\n`;
            healthReport += `4. Verify: MATCH ()-[r]->() RETURN count(r);\n`;
            healthReport += `\nSee reload-database.md for detailed instructions.`;
        } else {
            showToast('⚠️ Health check encountered errors', 'error', 7000);
        }

    } catch (error) {
        console.error('Health check error:', error);
        const errorMsg = `Database health check failed: ${error.message}`;
        showToast(errorMsg, 'error', 10000);
    } finally {
        btn.innerHTML = originalText;
    }
}

// Compare algorithms
async function compareAlgorithms() {
    const startNode = document.getElementById('startNode').value;
    const endNode = document.getElementById('endNode').value;

    if (!startNode || !endNode) {
        showStatus('comparisonStatus', '⚠️ Please select both start and end nodes.', 'error');
        return;
    }

    if (startNode === endNode) {
        showStatus('comparisonStatus', '⚠️ Start and end nodes must be different.', 'error');
        return;
    }

    const btn = document.querySelector('#compareBtnText');
    const originalText = btn.innerHTML;

    try {
        btn.innerHTML = 'Running... <span class="loading"></span>';
        document.querySelectorAll('button').forEach(b => b.disabled = true);

        showStatus('comparisonStatus', 'Running comparison...', 'info');
        document.getElementById('resultsSection').style.display = 'none';

        const response = await fetch(
            `${API_BASE}/api/comparison/compare-auto?from=${encodeURIComponent(startNode)}&to=${encodeURIComponent(endNode)}`
        );

        if (!response.ok) {
            let errorMessage = `Comparison failed with status ${response.status}`;
            try {
                const data = await response.json();
                errorMessage = data.message || data.error || errorMessage;
                console.error('Comparison error:', data);
            } catch (e) {
                errorMessage = `${errorMessage}: ${response.statusText}`;
            }
            showStatus('comparisonStatus', '❌ Comparison failed: ' + errorMessage, 'error');
            showToast('Comparison Failed: ' + errorMessage, 'error', 10000);
            return;
        }

        const data = await response.json();
        console.log('Received comparison data:', data); // Debug log

        showStatus('comparisonStatus', '✅ Comparison completed!', 'success');
        displayResults(data);
    } catch (error) {
        const errorMsg = `Network error during comparison: ${error.message}`;
        showStatus('comparisonStatus', '❌ ' + errorMsg, 'error');
        showToast(errorMsg, 'error', 10000);
        console.error('Comparison exception:', error);
    } finally {
        btn.innerHTML = originalText;
        document.querySelectorAll('button').forEach(b => b.disabled = false);
    }
}

// Display comparison results
function displayResults(data) {
    console.log('=== RAW DATA RECEIVED ===');
    console.log('Full data:', JSON.stringify(data, null, 2));
    console.log('aStarPath type:', typeof data.aStarPath);
    console.log('aStarPath isArray:', Array.isArray(data.aStarPath));
    console.log('aStarPath value:', data.aStarPath);
    console.log('========================');

    document.getElementById('resultsSection').style.display = 'block';

    // Winner
    document.getElementById('winner').textContent = data.winner || 'Unknown';

    // A* Results - try multiple property variations
    const aStarPath = data.aStarPath || data.astarPath || data.AStarPath;
    const aStarDistance = data.aStarTotalDistance ?? data.astarTotalDistance ?? data.AStarTotalDistance;
    const aStarTime = data.aStarExecutionTimeNanos ?? data.astarExecutionTimeNanos ?? data.AStarExecutionTimeNanos;
    const aStarVisited = data.aStarNodesVisited ?? data.astarNodesVisited ?? data.AStarNodesVisited;

    console.log('Extracted A* values:', { aStarPath, aStarDistance, aStarTime, aStarVisited });

    if (aStarPath && Array.isArray(aStarPath) && aStarPath.length > 0) {
        document.getElementById('astarNodes').textContent = `${aStarPath.length} nodes`;
        document.getElementById('astarDistance').textContent =
            aStarDistance !== undefined && aStarDistance !== null ? `${aStarDistance.toFixed(2)} km` : '-';
        document.getElementById('astarTime').textContent =
            aStarTime !== undefined && aStarTime !== null ? `${(aStarTime / 1000000).toFixed(2)} ms` : '-';
        document.getElementById('astarVisited').textContent =
            aStarVisited !== undefined && aStarVisited !== null ? aStarVisited : '-';
        document.getElementById('astarPath').innerHTML = formatPath(aStarPath);
    } else {
        console.warn('A* path is invalid:', aStarPath);
        document.getElementById('astarNodes').textContent = '-';
        document.getElementById('astarDistance').textContent = '-';
        document.getElementById('astarTime').textContent = '-';
        document.getElementById('astarVisited').textContent = '-';
        document.getElementById('astarPath').innerHTML = 'No data (check console for details)';
    }

    // AI Results
    const aiPath = data.aiPath || data.dqnPath;
    const aiDistance = data.aiTotalDistance ?? data.dqnTotalDistance;
    const aiTime = data.aiExecutionTimeNanos ?? data.dqnExecutionTimeNanos;
    const aiVisited = data.aiNodesVisited ?? data.dqnNodesVisited;

    if (aiPath && Array.isArray(aiPath) && aiPath.length > 0) {
        document.getElementById('aiNodes').textContent = `${aiPath.length} nodes`;
        document.getElementById('aiDistance').textContent =
            aiDistance !== undefined && aiDistance !== null ? `${aiDistance.toFixed(2)} km` : '-';
        document.getElementById('aiTime').textContent =
            aiTime !== undefined && aiTime !== null ? `${(aiTime / 1000000).toFixed(2)} ms` : '-';
        document.getElementById('aiVisited').textContent =
            aiVisited !== undefined && aiVisited !== null ? aiVisited : '-';
        document.getElementById('aiPath').innerHTML = formatPath(aiPath);
    } else {
        document.getElementById('aiNodes').textContent = 'No path found';
        document.getElementById('aiDistance').textContent = '-';
        document.getElementById('aiTime').textContent = '-';
        document.getElementById('aiVisited').textContent = '-';
        document.getElementById('aiPath').innerHTML = '<span style="color: red;">Failed to find path</span>';
    }

    // Analysis
    document.getElementById('analysisNotes').textContent = data.analysisNotes || 'No analysis available';

    // Scroll to results
    document.getElementById('resultsSection').scrollIntoView({ behavior: 'smooth' });
}

function formatPath(path) {
    if (!path || path.length === 0) return 'No path';

    return path.map((node, index) => {
        const arrow = index < path.length - 1 ? '<span class="path-arrow">→</span>' : '';
        return `<span class="path-node">${node.name}</span>${arrow}`;
    }).join('');
}

// Show status message
function showStatus(elementId, message, type) {
    const statusDiv = document.getElementById(elementId);
    statusDiv.textContent = message;
    statusDiv.className = `status ${type}`;

    // Show toast notification for errors and important messages
    if (type === 'error') {
        showToast(message, 'error');
    } else if (message.includes('failed') || message.includes('Failed')) {
        showToast(message, 'error');
    }
}

// Show toast notification popup
function showToast(message, type = 'info', duration = 5000) {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;

    // Choose icon based on type
    const icons = {
        'error': '❌',
        'success': '✅',
        'info': 'ℹ️',
        'warning': '⚠️'
    };

    toast.innerHTML = `
        <span class="toast-icon">${icons[type] || 'ℹ️'}</span>
        <div class="toast-message">${message}</div>
        <button class="toast-close" onclick="closeToast(this.parentElement)" aria-label="Close">×</button>
    `;

    container.appendChild(toast);

    // Auto-remove after duration
    setTimeout(() => {
        closeToast(toast);
    }, duration);
}

// Close toast notification
function closeToast(toast) {
    toast.classList.add('hiding');
    setTimeout(() => {
        toast.remove();
    }, 300);
}
