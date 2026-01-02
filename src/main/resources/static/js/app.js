// Main Application Logic
class McpClientApp {
    constructor() {
        this.currentSection = 'dashboard';
        this.modal = null;
        this.toast = null;
        this.currentStocks = [];
        this.currentPromptData = {
            'stock-analysis': null,
            'portfolio-review': null,
            'investment-advice': null
        };
        this.historyCurrentPage = 0;
        this.historyPageSize = 25;
        this.historySortDirection = 'desc';
        this.historyData = [];
        this.viewDetailsModal = null;
        this.currentSymbol = null;
        this.priceChart = null;
        this.indicatorChart = null;
        this.init();
    }

    init() {
        // Initialize Bootstrap components
        this.modal = new bootstrap.Modal(document.getElementById('addStockModal'));
        this.toast = document.getElementById('notification-toast');
        this.viewDetailsModal = new bootstrap.Modal(document.getElementById('viewDetailsModal'));

        // Setup navigation
        this.setupNavigation();

        // Load initial data
        this.loadDashboard();
        this.loadSystemInfo();
    }

    // ==================== Navigation ====================

    setupNavigation() {
        const navLinks = document.querySelectorAll('.sidebar .nav-link');
        navLinks.forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const section = link.getAttribute('data-section');
                this.navigateTo(section);

                // Update active link
                navLinks.forEach(l => l.classList.remove('active'));
                link.classList.add('active');
            });
        });
    }

    navigateTo(section) {
        // Hide all sections
        document.querySelectorAll('.content-section').forEach(s => {
            s.style.display = 'none';
        });

        // Show selected section
        const sectionElement = document.getElementById(`${section}-section`);
        if (sectionElement) {
            sectionElement.style.display = 'block';
            this.currentSection = section;

            // Load data for section
            switch (section) {
                case 'dashboard':
                    this.loadDashboard();
                    break;
                case 'portfolio':
                    this.loadPortfolio();
                    break;
                case 'market':
                    // Market section loads on demand
                    break;
                case 'prompts':
                    this.loadPromptsSection();
                    break;
                case 'history':
                    this.loadHistory();
                    break;
                case 'resources':
                    // Resources section is interactive
                    break;
                case 'system':
                    this.loadSystemInfo();
                    break;
            }
        }
    }

    // ==================== Dashboard ====================

    async loadDashboard() {
        try {
            const [stocks, value, usage, health] = await Promise.all([
                apiClient.getAllStocks().catch(() => []),
                apiClient.getPortfolioValue().catch(() => ({ totalValue: 0 })),
                apiClient.getApiUsage().catch(() => ({ requestsToday: 0, dailyLimit: 800 })),
                apiClient.checkHealth().catch(() => ({ status: 'DOWN' }))
            ]);

            this.updateDashboard(stocks, value, usage, health);
        } catch (error) {
            console.error('Error loading dashboard:', error);
            this.showToast('Error loading dashboard: ' + error.message, 'error');
        }
    }

    updateDashboard(stocks, value, usage, health) {
        // Update total value
        const totalValue = value.totalValue || 0;
        document.getElementById('total-value').textContent = `$${totalValue.toFixed(2)}`;

        // Update stocks count
        const stocksArray = Array.isArray(stocks) ? stocks : [];
        document.getElementById('stocks-count').textContent = stocksArray.length;

        // Update API usage
        const requestsToday = usage.requestsToday || 0;
        const dailyLimit = usage.dailyLimit || 800;
        document.getElementById('api-usage').textContent = `${requestsToday}/${dailyLimit}`;

        // Update health status
        const healthStatus = health.status === 'UP' ? 'Connected' : 'Disconnected';
        document.getElementById('health-status').textContent = healthStatus;
    }

    async refreshDashboard() {
        await this.loadDashboard();
        this.showToast('Dashboard refreshed successfully', 'success');
    }

    // ==================== Portfolio ====================

    async loadPortfolio() {
        try {
            const stocks = await apiClient.getAllStocks();
            this.currentStocks = Array.isArray(stocks) ? stocks : [];
            this.renderPortfolioTable();
        } catch (error) {
            console.error('Error loading portfolio:', error);
            this.showToast('Error loading portfolio: ' + error.message, 'error');
        }
    }

    renderPortfolioTable() {
        const tbody = document.getElementById('portfolio-table-body');
        if (!this.currentStocks || this.currentStocks.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center">No stocks in portfolio</td></tr>';
            return;
        }

        tbody.innerHTML = this.currentStocks.map(stock => {
            // Server returns currentPrice and totalValue (already calculated)
            const price = stock.currentPrice || stock.price || 0;
            const value = stock.totalValue || 0;
            return `
                <tr>
                    <td><strong>${stock.symbol}</strong></td>
                    <td>${stock.name || 'N/A'}</td>
                    <td>$${price.toFixed(2)}</td>
                    <td>${stock.shares || 0}</td>
                    <td>$${value.toFixed(2)}</td>
                    <td>
                        <button class="btn btn-sm btn-danger" onclick="app.deleteStockConfirm('${stock.symbol}')">Delete</button>
                    </td>
                </tr>
            `;
        }).join('');
    }

    showAddStockModal() {
        // Clear form
        document.getElementById('modal-symbol').value = '';
        document.getElementById('modal-name').value = '';
        document.getElementById('modal-price').value = '';
        document.getElementById('modal-shares').value = '';
        this.modal.show();
    }

    async fetchQuoteForModal() {
        const symbol = document.getElementById('modal-symbol').value;
        if (!symbol) {
            this.showToast('Please enter a symbol first', 'warning');
            return;
        }

        try {
            const quote = await apiClient.getQuote(symbol);
            document.getElementById('modal-name').value = quote.name || '';
            document.getElementById('modal-price').value = quote.price || quote.close || '';
            this.showToast('Quote fetched successfully', 'success');
        } catch (error) {
            this.showToast('Error fetching quote: ' + error.message, 'error');
        }
    }

    async addStockSubmit() {
        const symbol = document.getElementById('modal-symbol').value;
        const name = document.getElementById('modal-name').value;
        const price = document.getElementById('modal-price').value;
        const shares = document.getElementById('modal-shares').value;

        if (!symbol) {
            this.showToast('Symbol is required', 'warning');
            return;
        }

        try {
            await apiClient.addStock(
                symbol,
                name || null,
                price ? parseFloat(price) : null,
                shares ? parseInt(shares) : null
            );
            this.modal.hide();
            await this.loadPortfolio();
            this.showToast(`Stock ${symbol} added successfully`, 'success');
        } catch (error) {
            this.showToast('Error adding stock: ' + error.message, 'error');
        }
    }

    async deleteStockConfirm(symbol) {
        if (confirm(`Are you sure you want to delete ${symbol}?`)) {
            try {
                await apiClient.deleteStock(symbol);
                await this.loadPortfolio();
                this.showToast(`Stock ${symbol} deleted successfully`, 'success');
            } catch (error) {
                this.showToast('Error deleting stock: ' + error.message, 'error');
            }
        }
    }

    async refreshPortfolio() {
        try {
            await apiClient.refreshAllPrices();
            await this.loadPortfolio();
            this.showToast('Prices refreshed successfully', 'success');
        } catch (error) {
            this.showToast('Error refreshing prices: ' + error.message, 'error');
        }
    }

    // ==================== Market Data ====================

    async searchSymbols() {
        const query = document.getElementById('search-symbols').value;
        if (!query) {
            this.showToast('Please enter a search query', 'warning');
            return;
        }

        try {
            const results = await apiClient.searchSymbols(query);
            this.renderSearchResults(results);
        } catch (error) {
            this.showToast('Error searching symbols: ' + error.message, 'error');
        }
    }

    renderSearchResults(results) {
        const container = document.getElementById('search-results');
        if (!results || !results.data || results.data.length === 0) {
            container.innerHTML = '<p>No results found</p>';
            return;
        }

        container.innerHTML = results.data.slice(0, 10).map(item => `
            <div class="search-result-item" onclick="app.loadStockDetails('${item.symbol}')">
                <strong>${item.symbol}</strong> - ${item.instrument_name}
                <br><small>${item.exchange} | ${item.country}</small>
            </div>
        `).join('');
    }

    async loadStockDetails(symbol) {
        try {
            this.currentSymbol = symbol;
            const quote = await apiClient.getQuote(symbol);
            this.renderQuoteDetails(quote);
            document.getElementById('stock-details').style.display = 'block';

            // Load historical data for chart
            // Note: Using 1day interval by default
            const historical = await apiClient.getHistoricalData(symbol, '1day', 30);
            this.renderPriceChart(historical);

            // Show technical indicators panel
            document.getElementById('indicators-panel').style.display = 'block';
        } catch (error) {
            this.showToast('Error loading stock details: ' + error.message, 'error');
        }
    }

    renderQuoteDetails(quote) {
        const price = parseFloat(quote.price || quote.close || 0);
        const html = `
            <h5>${quote.symbol} - ${quote.name || 'N/A'}</h5>
            <p><strong>Price:</strong> $${price.toFixed(2)}</p>
            <p><strong>Change:</strong> ${quote.change || 'N/A'} (${quote.percent_change || 'N/A'})</p>
            <p><strong>Volume:</strong> ${quote.volume || 'N/A'}</p>
        `;
        document.getElementById('quote-details').innerHTML = html;
    }

    renderPriceChart(data) {
        const canvas = document.getElementById('price-chart');
        const ctx = canvas.getContext('2d');

        // Clear existing chart
        if (this.priceChart) {
            this.priceChart.destroy();
        }

        if (!data || !data.values || data.values.length === 0) {
            ctx.clearRect(0, 0, canvas.width, canvas.height);
            return;
        }

        const labels = data.values.map(v => v.datetime);
        const prices = data.values.map(v => parseFloat(v.close));

        this.priceChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Price',
                    data: prices,
                    borderColor: 'rgb(75, 192, 192)',
                    tension: 0.1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                scales: {
                    y: {
                        beginAtZero: false
                    }
                }
            }
        });
    }

    // ==================== AI Analysis with LLM Integration ====================

    async loadPromptsSection() {
        // Load available LLM providers
        try {
            const providers = await apiClient.getAvailableProviders();
            this.populateProviderSelect(providers);
        } catch (error) {
            this.showToast('Error loading LLM providers: ' + error.message, 'error');
        }
    }

    populateProviderSelect(providers) {
        const select = document.getElementById('llm-provider-select');
        if (providers.length === 0) {
            select.innerHTML = '<option value="">No LLM providers configured</option>';
            this.showToast('No LLM providers configured. Please add API keys to application.properties', 'warning');
            return;
        }
        select.innerHTML = providers.map(provider => {
            const label = provider === 'claude' ? 'Anthropic Claude' :
                         provider === 'openai' ? 'OpenAI GPT' :
                         provider === 'ollama' ? 'Ollama (Local)' : provider;
            return `<option value="${provider}">${label}</option>`;
        }).join('');
    }

    async analyzeStockWithAI() {
        const symbol = document.getElementById('stock-analysis-symbol').value;
        if (!symbol) {
            this.showToast('Please enter a stock symbol', 'warning');
            return;
        }

        const provider = document.getElementById('llm-provider-select').value;
        if (!provider) {
            this.showToast('No LLM provider available', 'error');
            return;
        }

        const responseContainer = document.getElementById('stock-analysis-response');
        const loadingDiv = document.getElementById('stock-analysis-loading');
        const promptDiv = document.getElementById('stock-analysis-prompt');

        try {
            // Show loading
            responseContainer.style.display = 'none';
            loadingDiv.style.display = 'block';

            // Generate prompt from MCP server
            const promptResult = await apiClient.getStockAnalysisPrompt(symbol);

            // Show the prompt that will be sent
            promptDiv.querySelector('pre').textContent = promptResult.content;
            promptDiv.style.display = 'block';

            // Send to LLM
            const llmRequest = {
                provider: provider,
                prompt: promptResult.content
            };

            const llmResponse = await apiClient.generateAiResponse(llmRequest);

            // Display response
            loadingDiv.style.display = 'none';
            responseContainer.style.display = 'block';
            responseContainer.classList.add('has-response');
            responseContainer.innerHTML = `
                ${llmResponse.response}
                <div class="response-meta">
                    <strong>Model:</strong> ${llmResponse.model} |
                    <strong>Provider:</strong> ${llmResponse.provider} |
                    <strong>Tokens:</strong> ${llmResponse.tokensUsed} |
                    <strong>Time:</strong> ${llmResponse.responseTimeMs}ms
                </div>
            `;

            // Store prompt data for saving
            this.currentPromptData['stock-analysis'] = {
                promptType: 'stock-analysis',
                prompt: promptResult.content,
                provider: llmResponse.provider,
                model: llmResponse.model,
                response: llmResponse.response,
                tokensUsed: llmResponse.tokensUsed,
                responseTimeMs: llmResponse.responseTimeMs,
                inputParameters: { symbol: symbol }
            };

            // Show save button
            document.getElementById('stock-analysis-save-container').style.display = 'block';

            this.showToast('AI analysis completed', 'success');
        } catch (error) {
            loadingDiv.style.display = 'none';
            responseContainer.style.display = 'block';
            responseContainer.innerHTML = `<div class="text-danger">Error: ${error.message}</div>`;
            this.showToast('Error generating AI response: ' + error.message, 'error');
        }
    }

    async reviewPortfolioWithAI() {
        const focus = document.getElementById('portfolio-review-focus').value;
        const provider = document.getElementById('llm-provider-select').value;

        if (!provider) {
            this.showToast('No LLM provider available', 'error');
            return;
        }

        const responseContainer = document.getElementById('portfolio-review-response');
        const loadingDiv = document.getElementById('portfolio-review-loading');
        const promptDiv = document.getElementById('portfolio-review-prompt');

        try {
            // Show loading
            responseContainer.style.display = 'none';
            loadingDiv.style.display = 'block';

            // Generate prompt from MCP server
            const promptResult = await apiClient.getPortfolioReviewPrompt(focus || null);

            // Show the prompt that will be sent
            promptDiv.querySelector('pre').textContent = promptResult.content;
            promptDiv.style.display = 'block';

            // Send to LLM
            const llmRequest = {
                provider: provider,
                prompt: promptResult.content
            };

            const llmResponse = await apiClient.generateAiResponse(llmRequest);

            // Display response
            loadingDiv.style.display = 'none';
            responseContainer.style.display = 'block';
            responseContainer.classList.add('has-response');
            responseContainer.innerHTML = `
                ${llmResponse.response}
                <div class="response-meta">
                    <strong>Model:</strong> ${llmResponse.model} |
                    <strong>Provider:</strong> ${llmResponse.provider} |
                    <strong>Tokens:</strong> ${llmResponse.tokensUsed} |
                    <strong>Time:</strong> ${llmResponse.responseTimeMs}ms
                </div>
            `;

            // Store prompt data for saving
            this.currentPromptData['portfolio-review'] = {
                promptType: 'portfolio-review',
                prompt: promptResult.content,
                provider: llmResponse.provider,
                model: llmResponse.model,
                response: llmResponse.response,
                tokensUsed: llmResponse.tokensUsed,
                responseTimeMs: llmResponse.responseTimeMs,
                inputParameters: { focus: focus || null }
            };

            // Show save button
            document.getElementById('portfolio-review-save-container').style.display = 'block';

            this.showToast('Portfolio review completed', 'success');
        } catch (error) {
            loadingDiv.style.display = 'none';
            responseContainer.style.display = 'block';
            responseContainer.innerHTML = `<div class="text-danger">Error: ${error.message}</div>`;
            this.showToast('Error generating AI response: ' + error.message, 'error');
        }
    }

    async getInvestmentAdviceWithAI() {
        const amount = document.getElementById('investment-amount').value;
        const riskTolerance = document.getElementById('risk-tolerance').value;

        if (!amount) {
            this.showToast('Please enter an amount', 'warning');
            return;
        }

        const provider = document.getElementById('llm-provider-select').value;
        if (!provider) {
            this.showToast('No LLM provider available', 'error');
            return;
        }

        const responseContainer = document.getElementById('investment-advice-response');
        const loadingDiv = document.getElementById('investment-advice-loading');
        const promptDiv = document.getElementById('investment-advice-prompt');

        try {
            // Show loading
            responseContainer.style.display = 'none';
            loadingDiv.style.display = 'block';

            // Generate prompt from MCP server
            const promptResult = await apiClient.getInvestmentAdvicePrompt(
                parseFloat(amount),
                riskTolerance || null
            );

            // Show the prompt that will be sent
            promptDiv.querySelector('pre').textContent = promptResult.content;
            promptDiv.style.display = 'block';

            // Send to LLM
            const llmRequest = {
                provider: provider,
                prompt: promptResult.content
            };

            const llmResponse = await apiClient.generateAiResponse(llmRequest);

            // Display response
            loadingDiv.style.display = 'none';
            responseContainer.style.display = 'block';
            responseContainer.classList.add('has-response');
            responseContainer.innerHTML = `
                ${llmResponse.response}
                <div class="response-meta">
                    <strong>Model:</strong> ${llmResponse.model} |
                    <strong>Provider:</strong> ${llmResponse.provider} |
                    <strong>Tokens:</strong> ${llmResponse.tokensUsed} |
                    <strong>Time:</strong> ${llmResponse.responseTimeMs}ms
                </div>
            `;

            // Store prompt data for saving
            this.currentPromptData['investment-advice'] = {
                promptType: 'investment-advice',
                prompt: promptResult.content,
                provider: llmResponse.provider,
                model: llmResponse.model,
                response: llmResponse.response,
                tokensUsed: llmResponse.tokensUsed,
                responseTimeMs: llmResponse.responseTimeMs,
                inputParameters: {
                    amount: parseFloat(amount),
                    riskTolerance: riskTolerance || null
                }
            };

            // Show save button
            document.getElementById('investment-advice-save-container').style.display = 'block';

            this.showToast('Investment advice completed', 'success');
        } catch (error) {
            loadingDiv.style.display = 'none';
            responseContainer.style.display = 'block';
            responseContainer.innerHTML = `<div class="text-danger">Error: ${error.message}</div>`;
            this.showToast('Error generating AI response: ' + error.message, 'error');
        }
    }

    // ==================== Resources ====================

    async getStockResource() {
        const symbol = document.getElementById('stock-resource-symbol').value;
        if (!symbol) {
            this.showToast('Please enter a stock symbol', 'warning');
            return;
        }

        try {
            const result = await apiClient.getStockResource(symbol);
            const resultDiv = document.getElementById('stock-resource-result');
            resultDiv.querySelector('pre').textContent = result.content;
            resultDiv.style.display = 'block';
        } catch (error) {
            this.showToast('Error fetching resource: ' + error.message, 'error');
        }
    }

    async getPortfolioSummary() {
        try {
            const result = await apiClient.getPortfolioSummary();
            const resultDiv = document.getElementById('portfolio-summary-result');
            resultDiv.querySelector('pre').textContent = result.content;
            resultDiv.style.display = 'block';
        } catch (error) {
            this.showToast('Error fetching resource: ' + error.message, 'error');
        }
    }

    async getStockList() {
        try {
            const result = await apiClient.getStockList();
            const resultDiv = document.getElementById('stock-list-result');
            resultDiv.querySelector('pre').textContent = result.content;
            resultDiv.style.display = 'block';
        } catch (error) {
            this.showToast('Error fetching resource: ' + error.message, 'error');
        }
    }

    // ==================== System Info ====================

    async loadSystemInfo() {
        try {
            const [tools, prompts, resources, health] = await Promise.all([
                apiClient.listTools(),
                apiClient.listPrompts(),
                apiClient.listResources(),
                apiClient.checkHealth()
            ]);

            this.renderHealthStatus(health);
            this.renderToolsTable(tools);
            this.renderPromptsTable(prompts);
            this.renderResourcesTable(resources);
        } catch (error) {
            console.error('Error loading system info:', error);
            this.showToast('Error loading system info: ' + error.message, 'error');
        }
    }

    renderHealthStatus(health) {
        const statusClass = health.status === 'UP' ? 'health-up' : 'health-down';
        const html = `
            <p><strong>Status:</strong> <span class="${statusClass}">${health.status}</span></p>
            <p><strong>Message:</strong> ${health.message}</p>
            <p><strong>Tools:</strong> ${health.toolsCount}</p>
            <p><strong>Prompts:</strong> ${health.promptsCount}</p>
            <p><strong>Resources:</strong> ${health.resourcesCount}</p>
        `;
        document.getElementById('system-health').innerHTML = html;
    }

    renderToolsTable(tools) {
        const tbody = document.getElementById('tools-table');
        if (!tools || tools.length === 0) {
            tbody.innerHTML = '<tr><td colspan="2">No tools available</td></tr>';
            return;
        }

        tbody.innerHTML = tools.map(tool => `
            <tr>
                <td><code>${tool.name}</code></td>
                <td>${tool.description || 'No description'}</td>
            </tr>
        `).join('');
    }

    renderPromptsTable(prompts) {
        const tbody = document.getElementById('prompts-table');
        if (!prompts || prompts.length === 0) {
            tbody.innerHTML = '<tr><td colspan="2">No prompts available</td></tr>';
            return;
        }

        tbody.innerHTML = prompts.map(prompt => `
            <tr>
                <td><code>${prompt.name}</code></td>
                <td>${prompt.description || 'No description'}</td>
            </tr>
        `).join('');
    }

    renderResourcesTable(resources) {
        const tbody = document.getElementById('resources-table');
        if (!resources || resources.length === 0) {
            tbody.innerHTML = '<tr><td colspan="3">No resources available</td></tr>';
            return;
        }

        tbody.innerHTML = resources.map(resource => `
            <tr>
                <td><code>${resource.name}</code></td>
                <td>${resource.description || 'No description'}</td>
                <td><code>${resource.uri}</code></td>
            </tr>
        `).join('');
    }

    // ==================== Utility ====================

    showToast(message, type = 'info') {
        const toastEl = this.toast;
        const toastBody = toastEl.querySelector('.toast-body');

        // Set message
        toastBody.textContent = message;

        // Set color based on type
        toastEl.className = 'toast';
        if (type === 'success') {
            toastEl.classList.add('bg-success', 'text-white');
        } else if (type === 'error') {
            toastEl.classList.add('bg-danger', 'text-white');
        } else if (type === 'warning') {
            toastEl.classList.add('bg-warning');
        }

        // Show toast
        const bsToast = new bootstrap.Toast(toastEl);
        bsToast.show();
    }

    // ==================== Prompt History Methods ====================

    async savePromptResponse(promptType) {
        const promptData = this.currentPromptData[promptType];

        if (!promptData) {
            this.showToast('No prompt data to save', 'warning');
            return;
        }

        const saveButton = document.querySelector(`#${promptType}-save-container button`);
        const saveStatus = document.getElementById(`${promptType}-save-status`);

        try {
            saveButton.disabled = true;
            saveStatus.textContent = 'Saving...';
            saveStatus.className = 'ms-2 text-muted';

            const result = await apiClient.savePromptResponse(promptData);

            saveStatus.textContent = `Saved (ID: ${result.id})`;
            saveStatus.className = 'ms-2 text-success';
            saveButton.disabled = true;
            saveButton.textContent = 'Saved';

            this.showToast('Prompt saved successfully', 'success');

        } catch (error) {
            console.error('Error saving prompt:', error);
            saveStatus.textContent = 'Save failed';
            saveStatus.className = 'ms-2 text-danger';
            saveButton.disabled = false;
            this.showToast('Error saving prompt: ' + error.message, 'error');
        }
    }

    async loadHistory() {
        const loadingDiv = document.getElementById('history-loading');
        const tableBody = document.getElementById('history-table-body');

        try {
            loadingDiv.style.display = 'block';
            tableBody.innerHTML = '<tr><td colspan="8" class="text-center">Loading...</td></tr>';

            const response = await apiClient.getHistoryPaginated(
                this.historyCurrentPage,
                this.historyPageSize,
                this.historySortDirection
            );

            this.historyData = response.content;

            // Update stats
            document.getElementById('history-total').textContent = response.totalItems;
            document.getElementById('history-current-page').textContent = response.currentPage + 1;
            document.getElementById('history-total-pages').textContent = response.totalPages;

            const startItem = response.currentPage * response.pageSize + 1;
            const endItem = Math.min(startItem + response.content.length - 1, response.totalItems);
            document.getElementById('history-showing-range').textContent =
                response.totalItems > 0 ? `${startItem}-${endItem}` : '0-0';

            // Render table
            this.renderHistoryTable();

            // Render pagination
            this.renderHistoryPagination(response.totalPages, response.currentPage);

            loadingDiv.style.display = 'none';

        } catch (error) {
            loadingDiv.style.display = 'none';
            tableBody.innerHTML = `<tr><td colspan="8" class="text-center text-danger">Error: ${error.message}</td></tr>`;
            this.showToast('Error loading history: ' + error.message, 'error');
        }
    }

    renderHistoryTable() {
        const tbody = document.getElementById('history-table-body');

        if (this.historyData.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted">No history records found</td></tr>';
            return;
        }

        tbody.innerHTML = this.historyData.map(record => {
            const date = new Date(record.timestamp);
            const formattedDate = date.toLocaleString();
            const typeLabel = this.formatPromptType(record.promptType);

            return `
                <tr>
                    <td>${record.id}</td>
                    <td>${formattedDate}</td>
                    <td><span class="badge bg-info">${typeLabel}</span></td>
                    <td>${record.provider}</td>
                    <td>${record.model}</td>
                    <td>${record.tokensUsed || 'N/A'}</td>
                    <td>${record.responseTimeMs ? record.responseTimeMs + ' ms' : 'N/A'}</td>
                    <td>
                        <button class="btn btn-sm btn-primary" onclick="app.viewHistoryDetails(${record.id})">
                            View
                        </button>
                        <button class="btn btn-sm btn-danger" onclick="app.deleteHistoryRecord(${record.id})">
                            Delete
                        </button>
                    </td>
                </tr>
            `;
        }).join('');
    }

    formatPromptType(type) {
        const types = {
            'stock-analysis': 'Stock Analysis',
            'portfolio-review': 'Portfolio Review',
            'investment-advice': 'Investment Advice'
        };
        return types[type] || type;
    }

    renderHistoryPagination(totalPages, currentPage) {
        const pagination = document.getElementById('history-pagination');

        if (totalPages <= 1) {
            pagination.innerHTML = '';
            return;
        }

        let html = '';

        // Previous button
        html += `
            <li class="page-item ${currentPage === 0 ? 'disabled' : ''}">
                <a class="page-link" href="#" onclick="app.goToHistoryPage(${currentPage - 1}); return false;">Previous</a>
            </li>
        `;

        // Page numbers
        for (let i = 0; i < totalPages; i++) {
            if (totalPages > 10 && i > 2 && i < totalPages - 3 && Math.abs(i - currentPage) > 2) {
                if (i === 3 || i === totalPages - 4) {
                    html += `<li class="page-item disabled"><a class="page-link">...</a></li>`;
                }
                continue;
            }

            html += `
                <li class="page-item ${i === currentPage ? 'active' : ''}">
                    <a class="page-link" href="#" onclick="app.goToHistoryPage(${i}); return false;">${i + 1}</a>
                </li>
            `;
        }

        // Next button
        html += `
            <li class="page-item ${currentPage === totalPages - 1 ? 'disabled' : ''}">
                <a class="page-link" href="#" onclick="app.goToHistoryPage(${currentPage + 1}); return false;">Next</a>
            </li>
        `;

        pagination.innerHTML = html;
    }

    goToHistoryPage(page) {
        this.historyCurrentPage = page;
        this.loadHistory();
    }

    changePageSize() {
        this.historyPageSize = parseInt(document.getElementById('history-page-size').value);
        this.historyCurrentPage = 0;
        this.loadHistory();
    }

    toggleHistorySort() {
        this.historySortDirection = this.historySortDirection === 'desc' ? 'asc' : 'desc';
        const btn = document.getElementById('history-sort-btn');
        const icon = this.historySortDirection === 'desc' ? 'sort-down' : 'sort-up';
        const text = this.historySortDirection === 'desc' ? 'Newest First' : 'Oldest First';
        btn.innerHTML = `<i class="bi bi-${icon}"></i> Sort: ${text}`;
        this.historyCurrentPage = 0;
        this.loadHistory();
    }

    async viewHistoryDetails(id) {
        try {
            const record = await apiClient.getHistoryById(id);

            document.getElementById('detail-id').textContent = record.id;
            document.getElementById('detail-date').textContent = new Date(record.timestamp).toLocaleString();
            document.getElementById('detail-type').textContent = this.formatPromptType(record.promptType);
            document.getElementById('detail-provider').textContent = record.provider;
            document.getElementById('detail-model').textContent = record.model;
            document.getElementById('detail-tokens').textContent = record.tokensUsed || 'N/A';
            document.getElementById('detail-time').textContent = record.responseTimeMs || 'N/A';
            document.getElementById('detail-params').textContent = JSON.stringify(record.inputParameters, null, 2);
            document.getElementById('detail-prompt').textContent = record.prompt;
            document.getElementById('detail-response').textContent = record.response;

            this.viewDetailsModal.show();

        } catch (error) {
            this.showToast('Error loading details: ' + error.message, 'error');
        }
    }

    async deleteHistoryRecord(id) {
        if (!confirm('Are you sure you want to delete this history record?')) {
            return;
        }

        try {
            await apiClient.deleteHistory(id);
            this.showToast('History record deleted successfully', 'success');
            this.loadHistory();
        } catch (error) {
            this.showToast('Error deleting record: ' + error.message, 'error');
        }
    }

    // ==================== Technical Indicator Methods ====================

    async loadIndicator(type, period) {
        if (!this.currentSymbol) {
            this.showToast('No stock selected', 'error');
            return;
        }

        try {
            let data;
            let indicatorName;

            switch(type) {
                case 'sma':
                    data = await apiClient.getSMA(this.currentSymbol, period);
                    indicatorName = `SMA (${period})`;
                    break;
                case 'ema':
                    data = await apiClient.getEMA(this.currentSymbol, period);
                    indicatorName = `EMA (${period})`;
                    break;
                case 'rsi':
                    data = await apiClient.getRSI(this.currentSymbol, period);
                    indicatorName = `RSI (${period})`;
                    break;
                case 'macd':
                    data = await apiClient.getMACD(this.currentSymbol);
                    indicatorName = 'MACD';
                    break;
                case 'bbands':
                    data = await apiClient.getBollingerBands(this.currentSymbol, period);
                    indicatorName = `Bollinger Bands (${period})`;
                    break;
                default:
                    this.showToast('Unknown indicator type', 'error');
                    return;
            }

            this.renderIndicatorChart(data, indicatorName, type);
            this.renderIndicatorDetails(data, indicatorName, type);

        } catch (error) {
            this.showToast('Error loading indicator: ' + error.message, 'error');
        }
    }

    renderIndicatorChart(data, name, type) {
        const canvas = document.getElementById('indicator-chart');
        const ctx = canvas.getContext('2d');

        // Clear existing chart
        if (this.indicatorChart) {
            this.indicatorChart.destroy();
        }

        if (!data || !data.technicalAnalysis) {
            ctx.clearRect(0, 0, canvas.width, canvas.height);
            return;
        }

        // Extract the indicator data key (e.g., "Technical Analysis: SMA")
        const analysisKey = Object.keys(data.technicalAnalysis).find(key =>
            key.startsWith('Technical Analysis:')
        );

        if (!analysisKey) {
            this.showToast('Invalid indicator data format', 'error');
            return;
        }

        const indicatorData = data.technicalAnalysis[analysisKey];
        const dates = Object.keys(indicatorData).reverse().slice(0, 100); // Last 100 points

        let datasets = [];

        if (type === 'sma' || type === 'ema') {
            // Single line for SMA/EMA
            const values = dates.map(date => parseFloat(indicatorData[date][type.toUpperCase()]));
            datasets.push({
                label: name,
                data: values,
                borderColor: 'rgb(75, 192, 192)',
                tension: 0.1,
                fill: false
            });

        } else if (type === 'rsi') {
            // RSI with overbought/oversold lines
            const values = dates.map(date => parseFloat(indicatorData[date].RSI));
            datasets.push({
                label: 'RSI',
                data: values,
                borderColor: 'rgb(54, 162, 235)',
                tension: 0.1,
                fill: false
            });
            // Overbought line (70)
            datasets.push({
                label: 'Overbought (70)',
                data: Array(dates.length).fill(70),
                borderColor: 'rgba(255, 99, 132, 0.5)',
                borderDash: [5, 5],
                fill: false
            });
            // Oversold line (30)
            datasets.push({
                label: 'Oversold (30)',
                data: Array(dates.length).fill(30),
                borderColor: 'rgba(75, 192, 192, 0.5)',
                borderDash: [5, 5],
                fill: false
            });

        } else if (type === 'macd') {
            // MACD: MACD line, Signal line, Histogram
            const macdValues = dates.map(date => parseFloat(indicatorData[date].MACD));
            const signalValues = dates.map(date => parseFloat(indicatorData[date].MACD_Signal));
            const histValues = dates.map(date => parseFloat(indicatorData[date].MACD_Hist));

            datasets.push({
                label: 'MACD',
                data: macdValues,
                borderColor: 'rgb(54, 162, 235)',
                tension: 0.1,
                fill: false
            });
            datasets.push({
                label: 'Signal',
                data: signalValues,
                borderColor: 'rgb(255, 159, 64)',
                tension: 0.1,
                fill: false
            });
            datasets.push({
                label: 'Histogram',
                data: histValues,
                type: 'bar',
                backgroundColor: 'rgba(75, 192, 192, 0.5)'
            });

        } else if (type === 'bbands') {
            // Bollinger Bands: Upper, Middle, Lower
            const upperValues = dates.map(date => parseFloat(indicatorData[date].Real_Upper_Band));
            const middleValues = dates.map(date => parseFloat(indicatorData[date].Real_Middle_Band));
            const lowerValues = dates.map(date => parseFloat(indicatorData[date].Real_Lower_Band));

            datasets.push({
                label: 'Upper Band',
                data: upperValues,
                borderColor: 'rgba(255, 99, 132, 0.8)',
                tension: 0.1,
                fill: false
            });
            datasets.push({
                label: 'Middle Band (SMA)',
                data: middleValues,
                borderColor: 'rgb(54, 162, 235)',
                tension: 0.1,
                fill: false
            });
            datasets.push({
                label: 'Lower Band',
                data: lowerValues,
                borderColor: 'rgba(75, 192, 192, 0.8)',
                tension: 0.1,
                fill: '+1'
            });
        }

        this.indicatorChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: dates,
                datasets: datasets
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    title: {
                        display: true,
                        text: `${this.currentSymbol} - ${name}`
                    },
                    legend: {
                        display: true
                    }
                },
                scales: {
                    y: {
                        beginAtZero: false
                    }
                }
            }
        });
    }

    renderIndicatorDetails(data, name, type) {
        const detailsDiv = document.getElementById('indicator-details');

        if (!data || !data.technicalAnalysis) {
            detailsDiv.innerHTML = '<i class="bi bi-info-circle"></i> No data available';
            detailsDiv.className = 'alert alert-warning';
            return;
        }

        const analysisKey = Object.keys(data.technicalAnalysis).find(key =>
            key.startsWith('Technical Analysis:')
        );
        const indicatorData = data.technicalAnalysis[analysisKey];
        const latestDate = Object.keys(indicatorData)[0]; // Most recent date
        const latestValues = indicatorData[latestDate];

        let interpretation = '';
        let alertClass = 'alert-info';

        if (type === 'sma' || type === 'ema') {
            const value = latestValues[type.toUpperCase()];
            interpretation = `
                <strong>${name}</strong><br>
                Latest value: ${parseFloat(value).toFixed(2)}<br>
                <small class="text-muted">Moving averages help identify trend direction.
                Price above MA suggests uptrend, below suggests downtrend.</small>
            `;

        } else if (type === 'rsi') {
            const rsi = parseFloat(latestValues.RSI);
            if (rsi > 70) {
                interpretation = `
                    <strong>RSI: ${rsi.toFixed(2)}</strong> - <span class="badge bg-danger">Overbought</span><br>
                    <small>Stock may be overvalued. Consider this as a potential sell signal.</small>
                `;
                alertClass = 'alert-danger';
            } else if (rsi < 30) {
                interpretation = `
                    <strong>RSI: ${rsi.toFixed(2)}</strong> - <span class="badge bg-success">Oversold</span><br>
                    <small>Stock may be undervalued. Consider this as a potential buy signal.</small>
                `;
                alertClass = 'alert-success';
            } else {
                interpretation = `
                    <strong>RSI: ${rsi.toFixed(2)}</strong> - <span class="badge bg-secondary">Neutral</span><br>
                    <small>Stock is in neutral territory. No strong buy or sell signal.</small>
                `;
            }

        } else if (type === 'macd') {
            const macd = parseFloat(latestValues.MACD);
            const signal = parseFloat(latestValues.MACD_Signal);
            const hist = parseFloat(latestValues.MACD_Hist);

            if (hist > 0) {
                interpretation = `
                    <strong>MACD</strong><br>
                    MACD: ${macd.toFixed(4)}, Signal: ${signal.toFixed(4)}, Histogram: ${hist.toFixed(4)}<br>
                    <span class="badge bg-success">Bullish Signal</span><br>
                    <small>MACD above signal line suggests upward momentum.</small>
                `;
                alertClass = 'alert-success';
            } else {
                interpretation = `
                    <strong>MACD</strong><br>
                    MACD: ${macd.toFixed(4)}, Signal: ${signal.toFixed(4)}, Histogram: ${hist.toFixed(4)}<br>
                    <span class="badge bg-danger">Bearish Signal</span><br>
                    <small>MACD below signal line suggests downward momentum.</small>
                `;
                alertClass = 'alert-danger';
            }

        } else if (type === 'bbands') {
            const upper = parseFloat(latestValues.Real_Upper_Band);
            const middle = parseFloat(latestValues.Real_Middle_Band);
            const lower = parseFloat(latestValues.Real_Lower_Band);

            interpretation = `
                <strong>Bollinger Bands</strong><br>
                Upper: ${upper.toFixed(2)}, Middle: ${middle.toFixed(2)}, Lower: ${lower.toFixed(2)}<br>
                <small>Bands show volatility. Price near upper band may indicate overbought,
                near lower band may indicate oversold. Wider bands suggest higher volatility.</small>
            `;
        }

        detailsDiv.innerHTML = `<i class="bi bi-lightbulb"></i> ${interpretation}`;
        detailsDiv.className = `alert ${alertClass}`;
    }
}

// Initialize app when DOM is loaded
let app;
document.addEventListener('DOMContentLoaded', () => {
    app = new McpClientApp();
});
