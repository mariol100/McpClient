// API Client for MCP Client Web Application
class McpApiClient {
    constructor(baseUrl = '') {
        this.baseUrl = baseUrl;
    }

    // ==================== Portfolio Endpoints ====================

    async getAllStocks() {
        return this.get('/api/portfolio/stocks');
    }

    async getStock(symbol) {
        return this.get(`/api/portfolio/stocks/${symbol}`);
    }

    async addStock(symbol, name, price, shares) {
        const data = { symbol };
        if (name) data.name = name;
        if (price !== null && price !== undefined) data.price = price;
        if (shares !== null && shares !== undefined) data.shares = shares;
        return this.post('/api/portfolio/stocks', data);
    }

    async updatePrice(symbol, newPrice) {
        return this.put(`/api/portfolio/stocks/${symbol}/price`, { newPrice });
    }

    async updateShares(symbol, newShares) {
        return this.put(`/api/portfolio/stocks/${symbol}/shares`, { newShares });
    }

    async deleteStock(symbol) {
        return this.delete(`/api/portfolio/stocks/${symbol}`);
    }

    async searchStocks(pattern) {
        return this.get(`/api/portfolio/stocks/search?pattern=${encodeURIComponent(pattern)}`);
    }

    async getPortfolioValue() {
        return this.get('/api/portfolio/value');
    }

    // ==================== Market Data Endpoints ====================

    async getQuote(symbol) {
        return this.get(`/api/market/quote/${symbol}`);
    }

    async getHistoricalData(symbol, interval, outputSize) {
        let url = `/api/market/historical/${symbol}?interval=${interval}`;
        if (outputSize) url += `&outputSize=${outputSize}`;
        return this.get(url);
    }

    async refreshAllPrices() {
        return this.post('/api/market/refresh-prices');
    }

    async searchSymbols(query) {
        return this.get(`/api/market/search?query=${encodeURIComponent(query)}`);
    }

    async getApiUsage() {
        return this.get('/api/market/api-usage');
    }

    // ==================== Technical Indicator Endpoints ====================

    async getSMA(symbol, timePeriod = 20, interval = 'daily', seriesType = 'close') {
        let url = `/api/market/indicators/sma/${symbol}?timePeriod=${timePeriod}&interval=${interval}&seriesType=${seriesType}`;
        return this.get(url);
    }

    async getEMA(symbol, timePeriod = 20, interval = 'daily', seriesType = 'close') {
        let url = `/api/market/indicators/ema/${symbol}?timePeriod=${timePeriod}&interval=${interval}&seriesType=${seriesType}`;
        return this.get(url);
    }

    async getRSI(symbol, timePeriod = 14, interval = 'daily', seriesType = 'close') {
        let url = `/api/market/indicators/rsi/${symbol}?timePeriod=${timePeriod}&interval=${interval}&seriesType=${seriesType}`;
        return this.get(url);
    }

    async getMACD(symbol, interval = 'daily', seriesType = 'close') {
        let url = `/api/market/indicators/macd/${symbol}?interval=${interval}&seriesType=${seriesType}`;
        return this.get(url);
    }

    async getBollingerBands(symbol, timePeriod = 20, interval = 'daily', seriesType = 'close') {
        let url = `/api/market/indicators/bbands/${symbol}?timePeriod=${timePeriod}&interval=${interval}&seriesType=${seriesType}`;
        return this.get(url);
    }

    // ==================== Prompts Endpoints ====================

    async getStockAnalysisPrompt(symbol) {
        return this.get(`/api/prompts/stock-analysis/${symbol}`);
    }

    async getPortfolioReviewPrompt(focus) {
        let url = '/api/prompts/portfolio-review';
        if (focus) url += `?focus=${encodeURIComponent(focus)}`;
        return this.get(url);
    }

    async getInvestmentAdvicePrompt(amount, riskTolerance) {
        let url = `/api/prompts/investment-advice?amount=${amount}`;
        if (riskTolerance) url += `&riskTolerance=${encodeURIComponent(riskTolerance)}`;
        return this.get(url);
    }

    async getStockSignalAnalysisPrompt(symbol) {
        return this.get(`/api/prompts/stock-signal-analysis/${symbol}`);
    }

    // ==================== LLM Integration Endpoints ====================

    async generateAiResponse(llmRequest) {
        return this.post('/api/prompts/generate-ai-response', llmRequest);
    }

    async getAvailableProviders() {
        return this.get('/api/prompts/available-providers');
    }

    async savePromptResponse(saveRequest) {
        return this.post('/api/prompts/save', saveRequest);
    }

    async getPromptHistory(promptType = null) {
        const url = promptType
            ? `/api/prompts/history?promptType=${promptType}`
            : '/api/prompts/history';
        return this.get(url);
    }

    async getHistoryPaginated(page = 0, size = 25, sort = 'desc', promptType = null) {
        let url = `/api/prompts/history/paginated?page=${page}&size=${size}&sort=${sort}`;
        if (promptType) url += `&promptType=${promptType}`;
        return this.get(url);
    }

    async getHistoryById(id) {
        return this.get(`/api/prompts/history/${id}`);
    }

    async deleteHistory(id) {
        return this.delete(`/api/prompts/history/${id}`);
    }

    // ==================== Resources Endpoints ====================

    async getStockResource(symbol) {
        return this.get(`/api/resources/stock/${symbol}`);
    }

    async getPortfolioSummary() {
        return this.get('/api/resources/portfolio/summary');
    }

    async getStockList() {
        return this.get('/api/resources/portfolio/list');
    }

    // ==================== Metadata Endpoints ====================

    async listTools() {
        return this.get('/api/metadata/tools');
    }

    async listPrompts() {
        return this.get('/api/metadata/prompts');
    }

    async listResources() {
        return this.get('/api/metadata/resources');
    }

    async checkHealth() {
        return this.get('/api/metadata/health');
    }

    // ==================== HTTP Helper Methods ====================

    async get(url) {
        try {
            const response = await fetch(this.baseUrl + url, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            return await this.handleResponse(response);
        } catch (error) {
            console.error('GET request failed:', error);
            throw error;
        }
    }

    async post(url, data) {
        try {
            const response = await fetch(this.baseUrl + url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            });
            return await this.handleResponse(response);
        } catch (error) {
            console.error('POST request failed:', error);
            throw error;
        }
    }

    async put(url, data) {
        try {
            const response = await fetch(this.baseUrl + url, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            });
            return await this.handleResponse(response);
        } catch (error) {
            console.error('PUT request failed:', error);
            throw error;
        }
    }

    async delete(url) {
        try {
            const response = await fetch(this.baseUrl + url, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            if (!response.ok) {
                const error = await response.json().catch(() => ({ message: response.statusText }));
                throw new Error(error.message || `HTTP ${response.status}: ${response.statusText}`);
            }
            // DELETE might return 204 No Content
            if (response.status === 204) {
                return null;
            }
            return await response.json();
        } catch (error) {
            console.error('DELETE request failed:', error);
            throw error;
        }
    }

    async handleResponse(response) {
        if (!response.ok) {
            const error = await response.json().catch(() => ({ message: response.statusText }));
            throw new Error(error.message || `HTTP ${response.status}: ${response.statusText}`);
        }
        return await response.json();
    }
}

// Export for use in other scripts
const apiClient = new McpApiClient();
