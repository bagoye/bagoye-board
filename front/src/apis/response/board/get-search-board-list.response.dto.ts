import { BoardListItem } from "types/interface";
import ResponseDto from "../Response.dto";

export default interface GetSearchBoardListResponseDto extends ResponseDto {
    searchList: BoardListItem[];
}