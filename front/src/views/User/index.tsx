import React, { useEffect, useRef, useState } from 'react'
import './style.css'
import defaultProfileIamge from 'assets/image/default-profile-image.png'
import { useParams } from 'react-router-dom';

// component: 유저 화면 컴포넌트
export default function User() {

  // state: 이메일 상태
  const { userEmail } = useParams();

  // component: 유저 화면 상단 컴포넌트
  const UserTop = () => {

    // state: 이미지 파일 input 참조 상태
    const imageInputRef = useRef<HTMLInputElement | null>(null);

    // state: 마이페이지 여부 상태
    const [isMyPage, setMyPage] = useState<boolean>(false);

    // state: 닉네임 변경 여부 상태
    const [isNicknameChange, setNicknameChange] = useState<boolean>(false);

    // state: 닉네임 상태
    const [nickname, setNickname] = useState<string>('');

    // state: 변경 닉네임 상태
    const [changeNickname, setChangeNickname] = useState<string>('');

    // state: 프로필 이미지 상태
    const [profileImage, setProfileImage] = useState<string | null>(null);


    // effect: user email path variable 변경시 실행할 함수
    useEffect(() => {

      if (!userEmail) return;
      setNickname('예바고')
      setProfileImage('https://social-phinf.pstatic.net/20211222_99/1640142237713qesd6_JPEG/269657793_1120205248725732_9165206711725205178_n.jpg')

    }, [userEmail])

    // render: 유저 화면 상단 컴포넌트 렌더링
    return (
      <div id='user-top-wrapper'>
        <div className='user-top-container'>
          {isMyPage ?
            <div className='user-top-my-profile-image-box'>
              {profileImage !== null ?
                <div className='user-top-profile-image' style={{ backgroundImage: `url(${profileImage})` }}></div> :
                <div className='user-top-my-profile-image-nothing-box'>
                  <div className='icon-box-large'>
                    <div className='icon image-box-white-icon'></div>
                  </div>
                </div>
              }
              <input ref={imageInputRef} type='file' accept='image/*' style={{ display: 'none' }} />
            </div> :
            <div className='user-top-profile-image-box' style={{ backgroundImage: `url(${profileImage ? profileImage : defaultProfileIamge})` }}></div>
          }
          <div className='user-top-info-box'>
            <div className='user-top-info-nickname-box'>
              {isMyPage ?
                <>
                  {isNicknameChange ?
                    <input className='user-top-info-nickname-input' type='text' size={nickname.length + 1} value={changeNickname} /> :
                    <div className='user-top-info-nickname'>{nickname}</div>
                  }
                  <div className='icon-button'>
                    <div className='icon edit-light-icon'></div>
                  </div>
                </> :
                <div className='user-top-info-nickname'>{nickname}</div>
              }
            </div>
            <div className='user-top-info-email'>{'test@test.com'}</div>
          </div>
        </div>
      </div>
    )
  }

  // component: 유저 화면 하단 컴포넌트
  const UserBottom = () => {

    // render: 유저 화면 하단 컴포넌트 렌더링
    return (
      <div></div>
    )
  }

  // render: 유저 화면 컴포넌트 렌더링
  return (
    <>
      <UserTop />
      <UserBottom />
    </>
  )
}
