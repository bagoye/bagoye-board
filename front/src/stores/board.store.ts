import { title } from "process";
import { create } from "zustand";

interface BoardStore {
    title: string;
    content: string;
    boardImageFileList: File[];
    setTitle: (title: string) => void;
    setContent: (content: string) => void;
    setBoardImageFileList: (boardImageFileList: File[]) => void;
    resetSetBoard: () => void;
};

const useBoardStore = create<BoardStore>(set => ({
    title: "",
    content: "",
    boardImageFileList: [],
    setTitle: (title) => set(state => ({ ...state, title })),
    setContent: (content) => set(state => ({ ...state, content })),
    setBoardImageFileList: (boardImageFileList) => set(state => ({ ...state, boardImageFileList })),
    resetSetBoard: () => set(state => ({ ...state, title: '', content: '', boardImageFileList: [] }))
}));

export default useBoardStore;