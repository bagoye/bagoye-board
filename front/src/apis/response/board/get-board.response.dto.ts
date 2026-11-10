import { Board } from "types/interface";
import ResponseDto from "../Response.dto";

export default interface GetBoardResponseDto extends ResponseDto, Board {
    
}