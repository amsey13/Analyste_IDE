import axios from 'axios';


const apiClient = axios.create({
    baseURL: '/api',
   
    timeout: 5000,
    
    withCredentials: true,
    xsrfCookieName: 'XSRF-TOKEN', 
    xsrfHeaderName: 'X-XSRF-TOKEN',
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    }
});

export default apiClient;