import React from 'react';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import axios from 'axios';
import Header from './index';
import { useBoardStore, useLoginUserStore } from 'stores';

jest.mock('axios', () => ({ __esModule: true, default: { post: jest.fn() } }));
let mockAccessToken: string | undefined;
jest.mock('react-cookie', () => ({
    useCookies: () => [{ accessToken: mockAccessToken }, jest.fn()]
}));

beforeEach(() => {
    jest.clearAllMocks();
    mockAccessToken = 'expired-token';
    useBoardStore.setState({ title: 'Test title', content: 'Test content', boardImageFileList: [] });
    useLoginUserStore.setState({ loginUser: null });
});

function renderWriter() {
    render(
        <MemoryRouter initialEntries={['/board/write']}>
            <Routes>
                <Route path="/board/write" element={<Header />} />
                <Route path="/auth" element={<div>Authentication screen</div>} />
            </Routes>
        </MemoryRouter>
    );
}

test('passes the Axios 401 body through to the login redirect', async () => {
    (axios.post as jest.Mock).mockRejectedValue({
        response: { status: 401, data: { code: 'AF', message: 'Authorization Failed.' } }
    });
    renderWriter();
    fireEvent.click(screen.getByText('업로드'));
    await waitFor(() => expect(screen.getByText('Authentication screen')).toBeTruthy());
    expect(axios.post).toHaveBeenCalledWith(
        'http://localhost:8080/api/v1/board',
        { title: 'Test title', content: 'Test content', boardImageList: [] },
        { headers: { Authorization: 'Bearer expired-token' } }
    );
});

test('redirects to login without sending a request when the cookie has expired', async () => {
    mockAccessToken = undefined;
    renderWriter();
    fireEvent.click(screen.getByText('업로드'));
    await waitFor(() => expect(screen.getByText('Authentication screen')).toBeTruthy());
    expect(axios.post).not.toHaveBeenCalled();
});
