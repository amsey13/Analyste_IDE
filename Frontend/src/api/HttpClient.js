import axios from 'axios';

const apiClient = axios.create({
    baseURL: '/api',
   
    timeout: 60000,
    
    withCredentials: true,
    xsrfCookieName: 'XSRF-TOKEN', 
    xsrfHeaderName: 'X-XSRF-TOKEN',
    headers: {

        'Accept': 'application/json'
    }
});

export default apiClient;