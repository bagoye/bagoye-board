import { BoardListItem } from "types/interface";
import ResponseDto from "../Response.dto";

export default interface GetUserBoardListResponseDto extends ResponseDto {
    userBoardList: BoardListItem[];
}