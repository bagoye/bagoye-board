import ResponseDto from "../Response.dto";

export default interface GetPopularListResponseDto extends ResponseDto {
    popularWordList: string[];
}